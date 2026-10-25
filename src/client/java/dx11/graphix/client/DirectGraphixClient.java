package dx11.graphix.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectGraphixClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("DirectGraphix-DX12");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[DirectGraphix] DirectX 12 / Panama Grafik Motoru Hazırlandı.");

        // Test amaçlı pencere açıldıktan 3 saniye sonra DX12 motorunu başlat
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(3000);
                DirectX12PanamaEngine.init();
            } catch (Exception ignored) {}
        });
    }
}