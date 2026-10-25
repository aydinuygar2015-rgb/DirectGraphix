package dx11.graphix.client;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class DirectX12PanamaEngine {

    private static long hwnd = 0;
    private static MemorySegment d3d12Device = MemorySegment.NULL;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;

        try (Arena arena = Arena.ofConfined()) {
            Linker linker = Linker.nativeLinker();
            SymbolLookup user32 = SymbolLookup.libraryLookup("user32.dll", arena);

            // Aktif olan pencere kolunu (HWND) doğrudan işletim sisteminden çek
            MethodHandle getForegroundWindow = linker.downcallHandle(
                    user32.find("GetForegroundWindow").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS)
            );

            MemorySegment hwndSegment = (MemorySegment) getForegroundWindow.invokeExact();
            hwnd = hwndSegment.address();

            // Eğer HWND henüz ön planda değilse GetActiveWindow dene
            if (hwnd == 0) {
                MethodHandle getActiveWindow = linker.downcallHandle(
                        user32.find("GetActiveWindow").orElseThrow(),
                        FunctionDescriptor.of(ValueLayout.ADDRESS)
                );
                hwndSegment = (MemorySegment) getActiveWindow.invokeExact();
                hwnd = hwndSegment.address();
            }

            DirectGraphixClient.LOGGER.info("[DirectGraphix-DX12] HWND Pencere Adresi Yakalandı: 0x" + Long.toHexString(hwnd));

            if (hwnd != 0) {
                createD3D12Device(arena, linker);
            } else {
                DirectGraphixClient.LOGGER.warn("[DirectGraphix-DX12] HWND henüz hazır değil, tekrar denenecek.");
            }
        } catch (Throwable t) {
            DirectGraphixClient.LOGGER.error("[DirectGraphix-DX12] Direct3D 12 Başlatma Hatası: ", t);
        }
    }

    private static void createD3D12Device(Arena arena, Linker linker) throws Throwable {
        SymbolLookup d3d12 = SymbolLookup.libraryLookup("d3d12.dll", arena);
        MethodHandle d3d12CreateDevice = linker.downcallHandle(
                d3d12.find("D3D12CreateDevice").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT, // HRESULT
                        ValueLayout.ADDRESS,  // pAdapter
                        ValueLayout.JAVA_INT,  // Minimum Feature Level
                        ValueLayout.ADDRESS,  // riid
                        ValueLayout.ADDRESS   // ppDevice
                )
        );

        // ID3D12Device GUID: {189819f1-143d-4a6c-98a6-4e858021e4ce}
        MemorySegment deviceIID = arena.allocate(16);
        deviceIID.set(ValueLayout.JAVA_INT, 0, 0x189819f1);

        MemorySegment ppDevice = arena.allocate(ValueLayout.ADDRESS);

        int result = (int) d3d12CreateDevice.invokeExact(
                MemorySegment.NULL,
                0xc000, // D3D_FEATURE_LEVEL_12_0
                deviceIID,
                ppDevice
        );

        if (result == 0) { // S_OK
            d3d12Device = ppDevice.get(ValueLayout.ADDRESS, 0);
            initialized = true;
            DirectGraphixClient.LOGGER.info("==================================================");
            DirectGraphixClient.LOGGER.info(" BAŞARILI: Panama FFM İle Direct3D 12 Cihazı Kuruldu!");
            DirectGraphixClient.LOGGER.info("==================================================");
        } else {
            DirectGraphixClient.LOGGER.error("[DirectGraphix-DX12] D3D12 Cihazı Oluşturulamadı. HRESULT: 0x" + Integer.toHexString(result));
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }
}