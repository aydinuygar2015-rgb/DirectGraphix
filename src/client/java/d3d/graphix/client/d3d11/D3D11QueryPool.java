package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.commands.GpuQueryPool;

import java.util.Arrays;
import java.util.OptionalLong;

public class D3D11QueryPool implements GpuQueryPool {
	private final int size;

	public D3D11QueryPool(int size) {
		this.size = size;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public OptionalLong getValue(int index) {
		return OptionalLong.empty();
	}

	@Override
	public OptionalLong[] getValues(int index, int count) {
		OptionalLong[] values = new OptionalLong[count];
		Arrays.fill(values, OptionalLong.empty());
		return values;
	}

	@Override
	public void close() {
	}
}