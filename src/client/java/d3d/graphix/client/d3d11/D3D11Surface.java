package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.device.SurfaceException;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.GpuSurfaceBackend;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDLVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Pencereye bağlı DXGI swapchain. Şimdilik sadece ekranı renge boyar. */
public class D3D11Surface implements GpuSurfaceBackend {
	private static final Logger LOGGER = LoggerFactory.getLogger("direct-graphix");
	private static final Set<GpuSurface.PresentMode> SUPPORTED_PRESENT_MODES =
		EnumSet.of(GpuSurface.PresentMode.FIFO, GpuSurface.PresentMode.IMMEDIATE);

	private final D3D11Device device;
	private final long windowHandle;
	private final BooleanSupplier isIconified;
	private D3D11Swapchain swapchain;
	private int syncInterval = 1;
	private boolean presentFailureLogged;

	public D3D11Surface(D3D11Device device, long windowHandle, BooleanSupplier isIconified) {
		this.device = device;
		this.windowHandle = windowHandle;
		this.isIconified = isIconified;
		LOGGER.info("[DX11] yüzey oluşturuldu, pencere = {}", windowHandle);
	}

	private long getHwnd() {
		var props = SDLVideo.SDL_GetWindowProperties(this.windowHandle);
		return SDLProperties.SDL_GetPointerProperty(props, "SDL.window.win32.hwnd", 0L);
	}

	@Override
	public void configure(GpuSurface.Configuration config) throws SurfaceException {
		LOGGER.info("[DX11] yüzey ayarlandı: {}x{}, {}", config.width(), config.height(), config.presentMode());
		this.syncInterval = config.presentMode() == GpuSurface.PresentMode.FIFO ? 1 : 0;
		try {
			if (this.swapchain == null) {
				long hwnd = this.getHwnd();
				if (hwnd == 0L) {
					throw new SurfaceException("Pencerenin HWND değeri alınamadı");
				}
				this.swapchain = D3D11Swapchain.create(this.device, hwnd, config.width(), config.height());
			} else {
				this.swapchain.resize(config.width(), config.height());
			}
		} catch (SurfaceException e) {
			throw e;
		} catch (Throwable t) {
			throw new SurfaceException(t);
		}
	}

	@Override
	public boolean isSuboptimal() {
		return false;
	}

	@Override
	public void acquireNextTexture() throws SurfaceException {
		if (this.isIconified.getAsBoolean()) {
			throw new SurfaceException("Cannot acquire minimized window");
		}
	}

	@Override
	public void blitFromTexture(CommandEncoderBackend commandEncoder, GpuTextureView textureView) {
		if (this.swapchain != null) {
			// Nabız gibi atan mavi renk: ekranın canlı olduğunu gösterir.
			double t = System.nanoTime() / 1.0e9;
			float pulse = (float) (0.5 + 0.5 * Math.sin(t * 2.0));
			this.swapchain.clear(0.0f, 0.15f + 0.25f * pulse, 0.55f + 0.2f * pulse, 1.0f);
		}
	}

	@Override
	public void present() {
		if (this.swapchain != null) {
			int hr = this.swapchain.present(this.syncInterval);
			if (hr < 0 && !this.presentFailureLogged) {
				this.presentFailureLogged = true;
				LOGGER.error("[DX11] Present başarısız: HRESULT=0x{}", Integer.toHexString(hr));
			}
		}
	}

	@Override
	public Collection<GpuSurface.PresentMode> supportedPresentModes() {
		return SUPPORTED_PRESENT_MODES;
	}

	@Override
	public void close() {
		if (this.swapchain != null) {
			this.swapchain.close();
			this.swapchain = null;
		}
	}
}