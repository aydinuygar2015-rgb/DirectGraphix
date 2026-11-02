package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.api.device.GpuBackend;
import com.mojang.renderpearl.api.device.GpuDebugOptions;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.frontend.FrontendGpuDevice;
import org.lwjgl.sdl.SDLVideo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class D3D11Backend implements GpuBackend {
	private static final Logger LOGGER = LoggerFactory.getLogger("direct-graphix");

	@Override
	public String getName() {
		return "DirectX 11";
	}

	@Override
	public void loadLibrary() throws BackendCreationException {
		LOGGER.info("[DX11] loadLibrary çağrıldı");
		if (!D3D11Native.INSTANCE.isSupported()) {
			throw new BackendCreationException(
				"DirectX 11 sadece Windows'ta kullanılabilir",
				BackendCreationException.Reason.OTHER
			);
		}
	}

	@Override
	public void unloadLibrary() {
		LOGGER.info("[DX11] unloadLibrary çağrıldı");
	}

	@Override
	public long createWindow(String title, int width, int height, long flags) {
		LOGGER.info("[DX11] createWindow çağrıldı: {}x{}", width, height);
		return SDLVideo.SDL_CreateWindow(title, width, height, flags);
	}

	@Override
	public GpuDevice createDevice(GpuDebugOptions debugOptions) throws BackendCreationException {
		LOGGER.info("[DX11] createDevice çağrıldı");
		return new FrontendGpuDevice(new D3D11GpuDevice(debugOptions));
	}
}