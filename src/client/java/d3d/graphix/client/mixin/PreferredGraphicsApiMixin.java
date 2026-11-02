package d3d.graphix.client.mixin;

import com.mojang.renderpearl.api.device.GpuBackend;
import d3d.graphix.client.d3d11.D3D11Backend;
import net.minecraft.client.PreferredGraphicsApi;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(PreferredGraphicsApi.class)
public abstract class PreferredGraphicsApiMixin {

	@Shadow
	@Final
	@Mutable
	private static PreferredGraphicsApi[] $VALUES;

	@Invoker("<init>")
	static PreferredGraphicsApi graphix$new(String enumName, int ordinal, String serializedName, String key) {
		throw new AssertionError();
	}

	@Inject(
		method = "<clinit>",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.PUTSTATIC,
			target = "Lnet/minecraft/client/PreferredGraphicsApi;$VALUES:[Lnet/minecraft/client/PreferredGraphicsApi;",
			shift = At.Shift.AFTER
		)
	)
	private static void graphix$addDirectX11(CallbackInfo ci) {
		PreferredGraphicsApi[] old = $VALUES;
		PreferredGraphicsApi[] extended = Arrays.copyOf(old, old.length + 1);
		extended[old.length] = graphix$new(
			"DIRECTX11",
			old.length,
			"directx11",
			"options.graphicsApi.directx11"
		);
		$VALUES = extended;
	}

	// DirectX 11 seçiliyse, listenin en başına DX11 backend'ini koy.
	@Inject(method = "getBackendsToTry", at = @At("RETURN"), cancellable = true)
	private void graphix$addDirectX11Backend(CallbackInfoReturnable<GpuBackend[]> cir) {
		PreferredGraphicsApi self = (PreferredGraphicsApi) (Object) this;
		if ("directx11".equals(self.getSerializedName())) {
			GpuBackend[] original = cir.getReturnValue();
			GpuBackend[] result = new GpuBackend[original.length + 1];
			result[0] = new D3D11Backend();
			System.arraycopy(original, 0, result, 1, original.length);
			cir.setReturnValue(result);
		}
	}
}