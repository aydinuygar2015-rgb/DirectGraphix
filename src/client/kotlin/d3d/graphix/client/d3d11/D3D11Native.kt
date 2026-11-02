package d3d.graphix.client.d3d11

import org.slf4j.LoggerFactory
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_INT
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.nio.charset.StandardCharsets

class D3D11Device(
	val device: MemorySegment,
	val context: MemorySegment,
	val featureLevel: Int,
	val adapterName: String,
	val vendorId: Int,
	val dedicatedVideoMemory: Long
)

object D3D11Native {
	private val LOGGER = LoggerFactory.getLogger("direct-graphix")

	private const val D3D_DRIVER_TYPE_UNKNOWN = 0
	private const val D3D_DRIVER_TYPE_HARDWARE = 1
	private const val D3D11_SDK_VERSION = 7
	private const val D3D_FEATURE_LEVEL_11_0 = 0xB000

	private val linker: Linker = Linker.nativeLinker()

	private class AdapterChoice(
		val adapter: MemorySegment,
		val name: String,
		val vendorId: Int,
		val vram: Long
	)

	fun isSupported(): Boolean =
		System.getProperty("os.name").lowercase().contains("windows")

	fun release(obj: MemorySegment) = Com.release(obj)

	/** En çok ekran kartı belleğine (VRAM) sahip adaptörü seçer. */
	private fun pickAdapter(arena: Arena): AdapterChoice? {
		val dxgi = SymbolLookup.libraryLookup("dxgi.dll", Arena.global())
		val createFactory = linker.downcallHandle(
			dxgi.find("CreateDXGIFactory").orElseThrow(),
			FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
		)
		val outFactory = arena.allocate(ADDRESS)
		val hr = createFactory.invokeWithArguments(Com.iidDxgiFactory(arena), outFactory) as Int
		if (hr < 0) {
			LOGGER.warn("[DX11] CreateDXGIFactory başarısız (0x{}), varsayılan adaptör kullanılacak", Integer.toHexString(hr))
			return null
		}
		val factory = outFactory.get(ADDRESS, 0)

		var best: AdapterChoice? = null
		try {
			var index = 0
			while (true) {
				val outAdapter = arena.allocate(ADDRESS)
				// IDXGIFactory::EnumAdapters (vtable 7)
				val r = Com.hr(factory, 7, Com.fdHr(JAVA_INT, ADDRESS), index, outAdapter)
				if (r < 0) {
					break
				}
				val info = describe(arena, outAdapter.get(ADDRESS, 0))
				LOGGER.info("[DX11] adaptör {}: {} (VRAM {} MB)", index, info.name, info.vram / (1024 * 1024))

				val current = best
				if (current == null || info.vram > current.vram) {
					if (current != null) {
						Com.release(current.adapter)
					}
					best = info
				} else {
					Com.release(info.adapter)
				}
				index++
			}
		} finally {
			Com.release(factory)
		}
		return best
	}

	private fun describe(arena: Arena, adapter: MemorySegment): AdapterChoice {
		// DXGI_ADAPTER_DESC (x64: 304 bayt)
		val desc = arena.allocate(304, 8)
		// IDXGIAdapter::GetDesc (vtable 8)
		val hr = Com.hr(adapter, 8, Com.fdHr(ADDRESS), desc)
		if (hr < 0) {
			return AdapterChoice(adapter, "Unknown", 0, 0L)
		}
		val name = desc.getString(0, StandardCharsets.UTF_16LE)
		val vendor = desc.get(JAVA_INT, 256)
		val vram = desc.get(JAVA_LONG, 272)
		return AdapterChoice(adapter, name, vendor, vram)
	}

	fun createDevice(): D3D11Device {
		check(isSupported()) { "DirectX 11 sadece Windows'ta çalışır" }

		val lookup = SymbolLookup.libraryLookup("d3d11.dll", Arena.global())
		val createDevice = linker.downcallHandle(
			lookup.find("D3D11CreateDevice").orElseThrow(),
			FunctionDescriptor.of(
				JAVA_INT,   // HRESULT
				ADDRESS,    // pAdapter
				JAVA_INT,   // DriverType
				ADDRESS,    // Software
				JAVA_INT,   // Flags
				ADDRESS,    // pFeatureLevels
				JAVA_INT,   // FeatureLevels
				JAVA_INT,   // SDKVersion
				ADDRESS,    // ppDevice
				ADDRESS,    // pFeatureLevel
				ADDRESS     // ppImmediateContext
			)
		)

		Arena.ofConfined().use { arena ->
			val choice = pickAdapter(arena)
			try {
				val levels = arena.allocate(JAVA_INT)
				levels.set(JAVA_INT, 0, D3D_FEATURE_LEVEL_11_0)

				val outDevice = arena.allocate(ADDRESS)
				val outLevel = arena.allocate(JAVA_INT)
				val outContext = arena.allocate(ADDRESS)

				val hr = createDevice.invokeWithArguments(
					choice?.adapter ?: MemorySegment.NULL,
					if (choice != null) D3D_DRIVER_TYPE_UNKNOWN else D3D_DRIVER_TYPE_HARDWARE,
					MemorySegment.NULL,
					0,
					levels,
					1,
					D3D11_SDK_VERSION,
					outDevice,
					outLevel,
					outContext
				) as Int

				check(hr >= 0) { "D3D11CreateDevice başarısız: HRESULT=0x%08X".format(hr) }

				val result = D3D11Device(
					outDevice.get(ADDRESS, 0),
					outContext.get(ADDRESS, 0),
					outLevel.get(JAVA_INT, 0),
					choice?.name ?: "Unknown",
					choice?.vendorId ?: 0,
					choice?.vram ?: 0L
				)
				LOGGER.info("[DX11] seçilen GPU: {}", result.adapterName)
				return result
			} finally {
				if (choice != null) {
					Com.release(choice.adapter)
				}
			}
		}
	}
}