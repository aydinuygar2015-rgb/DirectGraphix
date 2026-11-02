package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.common.BaseGpuBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Şimdilik CPU belleğinde tutulan buffer. Sonra ID3D11Buffer'a bağlanacak. */
public class D3D11Buffer extends BaseGpuBuffer {
	private final ByteBuffer data;
	private boolean closed;

	public D3D11Buffer(int usage, long size) {
		super(usage, size);
		this.data = ByteBuffer.allocateDirect(Math.toIntExact(size)).order(ByteOrder.nativeOrder());
	}

	@Override
	public boolean isClosed() {
		return this.closed;
	}

	@Override
	public void close() {
		this.closed = true;
	}

	@Override
	public GpuBufferSlice.MappedView map(long offset, long length, boolean read, boolean write) {
		ByteBuffer view = this.data
			.slice(Math.toIntExact(offset), Math.toIntExact(length))
			.order(ByteOrder.nativeOrder());
		return new GpuBufferSlice.MappedView(new GpuBufferSlice(this, offset, length), view, () -> {
		});
	}

	void write(long offset, ByteBuffer source) {
		ByteBuffer destination = this.data.duplicate();
		destination.position(Math.toIntExact(offset));
		ByteBuffer src = source.duplicate();
		if (src.remaining() > destination.remaining()) {
			src.limit(src.position() + destination.remaining());
		}
		destination.put(src);
	}

	void copyFrom(long destinationOffset, D3D11Buffer source, long sourceOffset, long length) {
		ByteBuffer src = source.data.duplicate();
		src.position(Math.toIntExact(sourceOffset));
		src.limit(Math.toIntExact(sourceOffset + length));
		this.write(destinationOffset, src);
	}
}