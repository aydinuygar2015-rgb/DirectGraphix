package d3d.graphix.client.d3d11

import org.slf4j.LoggerFactory
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_FLOAT
import java.lang.foreign.ValueLayout.JAVA_INT

class D3D11Swapchain private constructor(
	private val device: D3D11Device,
	private val swapChain: MemorySegment,
	private var rtv: MemorySegment,
	var width: Int,
	var height: Int
) : AutoCloseable {

	companion object {
		private val LOGGER = LoggerFactory.getLogger("direct-graphix")
		private const val DXGI_FORMAT_R8G8B8A8_UNORM = 28

		private fun ok(hr: Int, what: String) {
			if (hr < 0) {
				throw IllegalStateException("$what başarısız: HRESULT=0x%08X".format(hr))
			}
		}

		@JvmStatic
		fun create(device: D3D11Device, hwnd: Long, width: Int, height: Int): D3D11Swapchain {
			Arena.ofConfined().use { arena ->
				// cihaz -> IDXGIDevice -> IDXGIAdapter -> IDXGIFactory
				val outDxgiDevice = arena.allocate(ADDRESS)
				ok(
					Com.hr(device.device, 0, Com.fdHr(ADDRESS, ADDRESS), Com.iidDxgiDevice(arena), outDxgiDevice),
					"QueryInterface(IDXGIDevice)"
				)
				val dxgiDevice = outDxgiDevice.get(ADDRESS, 0)

				var adapter = MemorySegment.NULL
				var factory = MemorySegment.NULL
				try {
					val outAdapter = arena.allocate(ADDRESS)
					ok(Com.hr(dxgiDevice, 7, Com.fdHr(ADDRESS), outAdapter), "IDXGIDevice.GetAdapter")
					adapter = outAdapter.get(ADDRESS, 0)

					val outFactory = arena.allocate(ADDRESS)
					ok(
						Com.hr(adapter, 6, Com.fdHr(ADDRESS, ADDRESS), Com.iidDxgiFactory(arena), outFactory),
						"IDXGIObject.GetParent(IDXGIFactory)"
					)
					factory = outFactory.get(ADDRESS, 0)

					// DXGI_SWAP_CHAIN_DESC (x64: 72 bayt)
					val desc = arena.allocate(72, 8)
					desc.set(JAVA_INT, 0, width)                      // BufferDesc.Width
					desc.set(JAVA_INT, 4, height)                     // BufferDesc.Height
					desc.set(JAVA_INT, 16, DXGI_FORMAT_R8G8B8A8_UNORM) // BufferDesc.Format
					desc.set(JAVA_INT, 28, 1)                         // SampleDesc.Count
					desc.set(JAVA_INT, 36, 0x20)                      // DXGI_USAGE_RENDER_TARGET_OUTPUT
					desc.set(JAVA_INT, 40, 1)                         // BufferCount
					desc.set(ADDRESS, 48, MemorySegment.ofAddress(hwnd)) // OutputWindow
					desc.set(JAVA_INT, 56, 1)                         // Windowed
					desc.set(JAVA_INT, 60, 0)                         // SwapEffect = DISCARD

					val outSwap = arena.allocate(ADDRESS)
					ok(
						Com.hr(factory, 10, Com.fdHr(ADDRESS, ADDRESS, ADDRESS), device.device, desc, outSwap),
						"IDXGIFactory.CreateSwapChain"
					)
					val swap = outSwap.get(ADDRESS, 0)

					// Alt+Enter ile DXGI'nin kendi tam ekranını kapat (SDL yönetiyor)
					Com.hr(factory, 8, Com.fdHr(ADDRESS, JAVA_INT), MemorySegment.ofAddress(hwnd), 2)

					val rtv = createRtv(arena, device, swap)
					LOGGER.info("[DX11] swapchain oluşturuldu: {}x{}", width, height)
					return D3D11Swapchain(device, swap, rtv, width, height)
				} finally {
					Com.release(factory)
					Com.release(adapter)
					Com.release(dxgiDevice)
				}
			}
		}

		private fun createRtv(arena: Arena, device: D3D11Device, swap: MemorySegment): MemorySegment {
			val outBuffer = arena.allocate(ADDRESS)
			ok(
				Com.hr(swap, 9, Com.fdHr(JAVA_INT, ADDRESS, ADDRESS), 0, Com.iidTexture2D(arena), outBuffer),
				"IDXGISwapChain.GetBuffer"
			)
			val buffer = outBuffer.get(ADDRESS, 0)
			try {
				val outRtv = arena.allocate(ADDRESS)
				ok(
					Com.hr(device.device, 9, Com.fdHr(ADDRESS, ADDRESS, ADDRESS), buffer, MemorySegment.NULL, outRtv),
					"ID3D11Device.CreateRenderTargetView"
				)
				return outRtv.get(ADDRESS, 0)
			} finally {
				Com.release(buffer)
			}
		}
	}

	/** Arka yüzeyi tek renge boyar. */
	fun clear(r: Float, g: Float, b: Float, a: Float) {
		Arena.ofConfined().use { arena ->
			val color = arena.allocate(16, 4)
			color.set(JAVA_FLOAT, 0, r)
			color.set(JAVA_FLOAT, 4, g)
			color.set(JAVA_FLOAT, 8, b)
			color.set(JAVA_FLOAT, 12, a)
			// ID3D11DeviceContext::ClearRenderTargetView (vtable 50)
			Com.call(device.context, 50, Com.fdVoid(ADDRESS, ADDRESS), rtv, color)
		}
	}

	/** IDXGISwapChain::Present (vtable 8). HRESULT döner. */
	fun present(syncInterval: Int): Int =
		Com.hr(swapChain, 8, Com.fdHr(JAVA_INT, JAVA_INT), syncInterval, 0)

	fun resize(newWidth: Int, newHeight: Int) {
		if (newWidth == width && newHeight == height) {
			return
		}
		Com.release(rtv)
		// IDXGISwapChain::ResizeBuffers (vtable 13)
		ok(
			Com.hr(swapChain, 13, Com.fdHr(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT), 0, newWidth, newHeight, 0, 0),
			"IDXGISwapChain.ResizeBuffers"
		)
		Arena.ofConfined().use { arena ->
			rtv = createRtv(arena, device, swapChain)
		}
		width = newWidth
		height = newHeight
		LOGGER.info("[DX11] swapchain yeniden boyutlandı: {}x{}", newWidth, newHeight)
	}

	override fun close() {
		Com.release(rtv)
		Com.release(swapChain)
	}
}