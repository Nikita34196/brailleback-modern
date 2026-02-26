package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.bluetooth.*;
import android.util.Log;
import java.io.OutputStream;
import java.util.UUID;

public class BrailleBackService extends AccessibilityService {
    private static final String TAG = "BrailleBackModern";
    // Альтернативный UUID для некоторых моделей HumanWare
    private static final UUID HW_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private BluetoothSocket socket;
    private OutputStream out;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        connect();
    }

    private void connect() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) return;

        for (BluetoothDevice device : adapter.getBondedDevices()) {
            if (device.getName() != null && (device.getName().contains("Human") || device.getName().contains("ELF"))) {
                new Thread(() -> {
                    try {
                        // Используем метод secure для Android 16
                        socket = device.createRfcommSocketToServiceRecord(HW_UUID);
                        socket.connect();
                        out = socket.getOutputStream();
                        
                        // Команда инициализации из оригинальных исходников EuroBraille
                        out.write(new byte[]{0x1B, 0x54}); // ESC T
                        out.flush();
                        Log.i(TAG, "CONNECTED TO HUMANWARE");
                    } catch (Exception e) {
                        Log.e(TAG, "Connection failed: " + e.getMessage());
                    }
                }).start();
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (out != null && event.getText() != null && !event.getText().isEmpty()) {
            try {
                String text = event.getText().get(0).toString();
                // Кодируем текст в 8 точек (упрощенно)
                byte[] data = driver.formatText(text);
                out.write(data);
                out.flush();
            } catch (Exception e) {
                out = null;
            }
        }
    }

    @Override public void onInterrupt() {}
}
