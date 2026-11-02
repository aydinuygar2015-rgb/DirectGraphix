package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.buffers.TransientMemory;
import com.mojang.renderpearl.api.commands.GpuFence;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.api.CommandEncoderBackend;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

public class D3D11CommandEncoder implements CommandEncoderBackend {
	private final D3D11TransientMemory transientMemory = new D3D11TransientMemory();

	@Override
	public void submit() {
	}

	@Override
	public TransientMemory transientMemory() {
		return this.transientMemory;
	}

	@Override
	public RenderPassBackend createRenderPass(RenderPassDescriptor descriptor) {
		return new D3D11RenderPass();
	}

	@Override
	public void submitRenderPass() {
	}

	@Override
	public void clearColorTexture(GpuTexture colorTexture, Vector4fc clearColor) {
	}

	@Override
	public void clearColorAndDepthTextures(GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth) {
	}

	@Override
	public void clearColorAndDepthTextures(
		GpuTexture colorTexture,
		Vector4fc clearColor,
		GpuTexture depthTexture,
		double clearDepth,
		int regionX,
		int regionY,
		int regionWidth,
		int regionHeight,
		int mipLevel
	) {
	}

	@Override
	public void clearDepthTexture(GpuTexture depthTexture, double clearDepth) {
	}

	@Override
	public void writeToBuffer(GpuBufferSlice destination, ByteBuffer data) {
		((D3D11Buffer) destination.buffer()).write(destination.offset(), data);
	}

	@Override
	public void copyToBuffer(GpuBufferSlice source, GpuBufferSlice target) {
		long length = Math.min(source.length(), target.length());
		((D3D11Buffer) target.buffer()).copyFrom(target.offset(), (D3D11Buffer) source.buffer(), source.offset(), length);
	}

	@Override
	public void writeToTexture(
		GpuTexture destination, ByteBuffer source, int mipLevel, int depthOrLayer, int destX, int destY, int width, int height
	) {
	}

	@Override
	public void copyBufferToTexture(
		GpuBufferSlice source,
		int sourceX,
		int sourceY,
		int sourceWidth,
		int sourceHeight,
		GpuTexture destination,
		int destinationX,
		int destinationY,
		int copyWidth,
		int copyHeight,
		int mipLevel,
		int arrayLayer
	) {
	}

	@Override
	public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel) {
		callback.run();
	}

	@Override
	public void copyTextureToBuffer(
		GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel, int x, int y, int width, int height
	) {
		callback.run();
	}

	@Override
	public void copyTextureToTexture(
		GpuTexture source, GpuTexture destination, int mipLevel, int destX, int destY, int sourceX, int sourceY, int width, int height
	) {
	}

	@Override
	public GpuFence createFence() {
		return new D3D11Fence();
	}

	@Override
	public void writeTimestamp(GpuQueryPool pool, int index) {
	}
}