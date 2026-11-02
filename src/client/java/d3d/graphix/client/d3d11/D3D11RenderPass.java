package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;

/** Şimdilik hiçbir şey çizmeyen render pass. */
public class D3D11RenderPass implements RenderPassBackend {
	@Override
	public void pushDebugGroup(Supplier<String> label) {
	}

	@Override
	public void popDebugGroup() {
	}

	@Override
	public void setPipeline(BackendRenderPipeline pipeline) {
	}

	@Override
	public void setUniform(int index, Object value) {
	}

	@Override
	public void pushConstants(ByteBuffer value) {
	}

	@Override
	public void enableScissor(int x, int y, int width, int height) {
	}

	@Override
	public void disableScissor() {
	}

	@Override
	public void setVertexBuffer(int slot, GpuBufferSlice vertexBuffer) {
	}

	@Override
	public void setIndexBuffer(GpuBuffer indexBuffer, IndexType indexType) {
	}

	@Override
	public void drawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) {
	}

	@Override
	public void multiDrawIndexed(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) {
	}

	@Override
	public void multiDrawIndexed(PointerBuffer firstIndexOffsets, IntBuffer indexCounts, IntBuffer vertexOffsets, int drawCount) {
	}

	@Override
	public void drawIndexedIndirect(GpuBufferSlice commands, int drawCount) {
	}

	@Override
	public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
	}

	@Override
	public void multiDraw(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) {
	}

	@Override
	public void multiDraw(IntBuffer firstVertices, IntBuffer vertexCounts, int drawCount) {
	}

	@Override
	public void drawIndirect(GpuBufferSlice commands, int drawCount) {
	}

	@Override
	public void writeTimestamp(GpuQueryPool pool, int index) {
	}
}