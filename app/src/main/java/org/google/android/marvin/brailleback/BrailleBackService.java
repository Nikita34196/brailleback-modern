package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.bluetooth.*;
import android.util.Log;
import java.io.OutputStream;
import java.util.UUID;

public class BrailleBackService extends AccessibilityService {
    private static final String TAG = "BrailleBackModern";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private BluetoothSocket socket;
    private OutputStream out;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Сервис запущен, пробую 'небезопасное' подключение...");
        connectInsecure();
    }

    private void connectInsecure() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) return;

        for (BluetoothDevice device : adapter.getBondedDevices()) {
            if (device.getName() != null && (device.getName().contains("Human") || device.getName().contains("ELF"))) {
                new Thread(() -> {
                    try {
                        // Используем Insecure метод - он чаще срабатывает на Android 16
                        socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                        socket.connect();
                        out = socket.getOutputStream();
                        
                        // Команда 'пробуждения' дисплея
                        out.write(new byte[]{0x1B, 0x54, 0x1B, 0x49}); 
                        out.flush();
                        Log.i(TAG, "ЕСТЬ КОНТАКТ! Дисплей подключен.");
                    } catch (Exception e) {
                        Log.e(TAG, "Сбой подключения: " + e.getMessage());
                        socket = null;
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
                out.write(driver.formatText(text));
                out.flush();
            } catch (Exception e) {
                out = null;
                connectInsecure(); // Пробуем переподключиться при обрыве
            }
        }
    }

    @Override public void onInterrupt() {}
}
