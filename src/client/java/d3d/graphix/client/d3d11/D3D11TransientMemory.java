package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.buffers.TransientMemory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/** Her ayırma için ayrı CPU buffer'ı veren basit sürüm. */
public class D3D11TransientMemory implements TransientMemory {
	private static long alignUp(long value, long alignment) {
		return alignment <= 1L ? value : (value + alignment - 1L) / alignment * alignment;
	}

	private static D3D11Buffer newBuffer(int usage, long size, long minimumAllocation) {
		return new D3D11Buffer(usage, Math.max(Math.max(size, minimumAllocation), 1L));
	}

	@Override
	public ByteBuffer allocateCpu(long size, long alignment, long minimumAllocation, long elementSize) {
		return ByteBuffer.allocateDirect(Math.toIntExact(Math.max(size, 1L))).order(ByteOrder.nativeOrder());
	}

	@Override
	public GpuBufferSlice.MappedView allocateStaging(long size, long alignment, int usage, long minimumAllocation, long elementSize) {
		return newBuffer(usage, size, minimumAllocation).map(0L, size, true, true);
	}

	@Override
	public GpuBufferSlice allocateGpu(long size, long alignment, int usage, long minimumAllocation, long elementSize) {
		return newBuffer(usage, size, minimumAllocation).slice(0L, size);
	}

	@Override
	public GpuBufferSlice.MappedView allocateGpuMapped(long size, long alignment, int usage, long minimumAllocation, long elementSize) {
		return newBuffer(usage, size, minimumAllocation).map(0L, size, true, true);
	}

	@Override
	public GpuBufferSlice uploadStaging(List<ByteBuffer> data, long alignment, int usage, long minimumAllocation, long elementSize) {
		return upload(data, usage, minimumAllocation);
	}

	@Override
	public GpuBufferSlice uploadGpu(List<ByteBuffer> data, long alignment, int usage, long minimumAllocation, long elementSize) {
		return upload(data, usage, minimumAllocation);
	}

	@Override
	public List<GpuBufferSlice> multiUploadStaging(List<ByteBuffer> data, long alignment, int usage) {
		return multiUpload(data, alignment, usage);
	}

	@Override
	public List<GpuBufferSlice> multiUploadGpu(List<ByteBuffer> data, long alignment, int usage) {
		return multiUpload(data, alignment, usage);
	}

	private static GpuBufferSlice upload(List<ByteBuffer> data, int usage, long minimumAllocation) {
		long total = 0L;
		for (ByteBuffer part : data) {
			total += part.remaining();
		}
		D3D11Buffer buffer = newBuffer(usage, total, minimumAllocation);
		long offset = 0L;
		for (ByteBuffer part : data) {
			buffer.write(offset, part);
			offset += part.remaining();
		}
		return buffer.slice(0L, total);
	}

	private static List<GpuBufferSlice> multiUpload(List<ByteBuffer> data, long alignment, int usage) {
		long total = 0L;
		for (ByteBuffer part : data) {
			total = alignUp(total, alignment) + part.remaining();
		}
		D3D11Buffer buffer = newBuffer(usage, total, 1L);
		List<GpuBufferSlice> result = new ArrayList<>();
		long offset = 0L;
		for (ByteBuffer part : data) {
			offset = alignUp(offset, alignment);
			buffer.write(offset, part);
			result.add(buffer.slice(offset, part.remaining()));
			offset += part.remaining();
		}
		return result;
	}
}