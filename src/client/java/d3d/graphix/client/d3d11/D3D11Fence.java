package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.commands.GpuFence;

public class D3D11Fence implements GpuFence {
	@Override
	public void close() {
	}

	@Override
	public boolean awaitCompletion(long timeoutNS) {
		return true;
	}
}