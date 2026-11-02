package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.common.BaseGpuTextureView;

public class D3D11TextureView extends BaseGpuTextureView {
	private boolean closed;

	public D3D11TextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
		super(texture, baseMipLevel, mipLevels);
	}

	@Override
	public boolean isClosed() {
		return this.closed;
	}

	@Override
	public void close() {
		this.closed = true;
	}
}