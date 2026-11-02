package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.backend.api.BackendRenderPipeline;

public class D3D11RenderPipeline implements BackendRenderPipeline {
	private final String name;
	private boolean closed;

	public D3D11RenderPipeline(String name) {
		this.name = name;
	}

	public String name() {
		return this.name;
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