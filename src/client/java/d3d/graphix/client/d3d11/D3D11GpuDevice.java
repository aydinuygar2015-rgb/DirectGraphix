package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.DeviceType;
import com.mojang.renderpearl.api.device.GpuDebugOptions;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;
import com.mojang.renderpearl.backend.api.GpuSurfaceBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class D3D11GpuDevice implements GpuDeviceBackend {
	private static final Logger LOGGER = LoggerFactory.getLogger("direct-graphix");

	private final D3D11Device nativeDevice;
	private final DeviceInfo deviceInfo;
	private final D3D11CommandEncoder encoder = new D3D11CommandEncoder();
	private boolean closed;

	public D3D11GpuDevice(GpuDebugOptions debugOptions) throws BackendCreationException {
		D3D11Device created;
		try {
			created = D3D11Native.INSTANCE.createDevice();
		} catch (Throwable t) {
			throw new BackendCreationException(
				"D3D11 cihazı oluşturulamadı: " + t,
				BackendCreationException.Reason.OTHER
			);
		}
		this.nativeDevice = created;

		DeviceType type = created.getDedicatedVideoMemory() > 1024L * 1024L * 1024L
			? DeviceType.DISCRETE
			: DeviceType.INTEGRATED;
		this.deviceInfo = D3D11DeviceInfoFactory.create(
			created.getAdapterName(),
			D3D11DeviceInfoFactory.vendorName(created.getVendorId()),
			created.getFeatureLevel(),
			type
		);
		LOGGER.info(
			"[DX11] GpuDeviceBackend hazır: {} (VRAM {} MB), feature level = 0x{}",
			created.getAdapterName(),
			created.getDedicatedVideoMemory() / (1024L * 1024L),
			Integer.toHexString(created.getFeatureLevel())
		);
	}

	@Override
	public GpuSurfaceBackend createSurface(long windowHandle, BooleanSupplier isIconified) {
		return new D3D11Surface(this.nativeDevice, windowHandle, isIconified);
	}

	@Override
	public CommandEncoderBackend createCommandEncoder() {
		return this.encoder;
	}

	@Override
	public GpuSampler createSampler(
		AddressMode addressModeU,
		AddressMode addressModeV,
		FilterMode minFilter,
		FilterMode magFilter,
		int maxAnisotropy,
		OptionalDouble maxLod
	) {
		return new D3D11Sampler(addressModeU, addressModeV, minFilter, magFilter, maxAnisotropy, maxLod);
	}

	@Override
	public GpuTexture createTexture(
		String label, int usage, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels
	) {
		return new D3D11Texture(usage, label == null ? "d3d11_texture" : label, format, width, height, depthOrLayers, mipLevels);
	}

	@Override
	public GpuTextureView createTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
		return new D3D11TextureView(texture, baseMipLevel, mipLevels);
	}

	@Override
	public GpuBuffer createBuffer(Supplier<String> label, int usage, long size) {
		return new D3D11Buffer(usage, size);
	}

	@Override
	public GpuBuffer createBuffer(Supplier<String> label, int usage, ByteBuffer data) {
		D3D11Buffer buffer = new D3D11Buffer(usage, data.remaining());
		buffer.write(0L, data);
		return buffer;
	}

	@Override
	public List<String> getLastDebugMessages() {
		return List.of();
	}

	@Override
	public boolean isDebuggingEnabled() {
		return false;
	}

	@Override
	public BackendRenderPipeline.Pending compilePipeline(BackendRenderPipeline.CreateInfo pipelineCreateInfo) {
		// HLSL aşamasında burada shader'lar yüklenecek. Şimdilik boş pipeline.
		return () -> new D3D11RenderPipeline(pipelineCreateInfo.name());
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
		D3D11Native.INSTANCE.release(this.nativeDevice.getContext());
		D3D11Native.INSTANCE.release(this.nativeDevice.getDevice());
		LOGGER.info("[DX11] cihaz kapatıldı");
	}

	@Override
	public GpuQueryPool createTimestampQueryPool(int size) {
		return new D3D11QueryPool(size);
	}

	@Override
	public long getTimestampCalibrationOffset() {
		return 0L;
	}

	@Override
	public DeviceInfo getDeviceInfo() {
		return this.deviceInfo;
	}
}