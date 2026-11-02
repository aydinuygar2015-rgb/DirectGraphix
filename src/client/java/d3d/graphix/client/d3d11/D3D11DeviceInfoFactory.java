package d3d.graphix.client.d3d11;

import com.mojang.renderpearl.api.device.DeviceFeatures;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.DeviceLimits;
import com.mojang.renderpearl.api.device.DeviceType;
import com.mojang.renderpearl.api.device.HintsAndWorkarounds;

import java.util.Set;

public final class D3D11DeviceInfoFactory {
	private D3D11DeviceInfoFactory() {
	}

	public static String vendorName(int vendorId) {
		return switch (vendorId) {
			case 0x10DE -> "NVIDIA";
			case 0x1002, 0x1022 -> "AMD";
			case 0x8086 -> "Intel";
			case 0x1414 -> "Microsoft";
			default -> "Unknown (0x" + Integer.toHexString(vendorId) + ")";
		};
	}

	public static DeviceInfo create(String adapterName, String vendorName, int featureLevel, DeviceType type) {
		DeviceLimits limits = new DeviceLimits(
			16,                          // maxAnisotropy
			256,                         // minUniformOffsetAlignment
			16384,                       // maxTextureSize (feature level 11_0)
			2048L * 1024L * 1024L,       // maxMemoryAllocationSize
			1,                           // maxMultiDrawDirectInterleavedDrawCount
			8,                           // maxColorAttachments
			1                            // maxDrawIndirectDrawCount
		);

		DeviceFeatures features = new DeviceFeatures(
			true,   // wireframeFillMode
			false,  // shaderDrawParameters
			false,  // multiDrawDirectInterleaved
			false,  // multiDrawDirectSeparate
			false,  // multiDrawIndirect
			true,   // drawIndirect
			true,   // nonZeroFirstInstance
			false   // persistentMapping
		);

		HintsAndWorkarounds hints = new HintsAndWorkarounds(false, false, false, false);

		return new DeviceInfo(
			adapterName,
			vendorName,
			"Direct3D 11, feature level 0x" + Integer.toHexString(featureLevel),
			true,
			"DirectX 11",
			1.0f,
			limits,
			features,
			Set.of("D3D11"),
			hints,
			type
		);
	}
}