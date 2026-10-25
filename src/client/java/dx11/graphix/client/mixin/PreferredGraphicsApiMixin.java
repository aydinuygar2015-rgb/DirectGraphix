package dx11.graphix.client.mixin;

import dx11.graphix.client.DirectX12PanamaEngine;
import dx11.graphix.client.DirectGraphixClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.PreferredGraphicsApi")
public class PreferredGraphicsApiMixin {

    @Inject(method = "byName", at = @At("HEAD"), cancellable = true)
    private static void onGetGraphicsApiByName(String name, CallbackInfoReturnable<?> cir) {
        if ("DIRECTX_12".equalsIgnoreCase(name) || "DX12".equalsIgnoreCase(name)) {
            DirectGraphixClient.LOGGER.info("[DirectGraphix] 'PreferredGraphicsApi' üzerinden DIRECTX_12 seçildi! Panama DX12 Motoru Tetikleniyor...");
            DirectX12PanamaEngine.init();
        }
    }
}