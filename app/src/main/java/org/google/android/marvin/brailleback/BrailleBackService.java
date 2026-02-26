package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.bluetooth.*;
import android.util.Log;
import java.io.OutputStream;
import java.util.UUID;

public class BrailleBackService extends AccessibilityService {
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
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
                        socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                        socket.connect();
                        out = socket.getOutputStream();
                        
                        // Посылаем пакет инициализации как в TalkBack
                        out.write(driver.getInitPacket());
                        out.flush();
                        Log.i("BrailleBack", "Драйвер TalkBack активирован");
                    } catch (Exception e) {
                        Log.e("BrailleBack", "Ошибка: " + e.getMessage());
                    }
                }).start();
                break;
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (out == null) return;
        // Берем текст из описания контента (более надежно для TalkBack)
        CharSequence content = event.getContentDescription();
        if (content == null && event.getText() != null && !event.getText().isEmpty()) {
            content = event.getText().get(0);
        }

        if (content != null) {
            try {
                out.write(driver.formatText(content.toString()));
                out.flush();
            } catch (Exception e) {
                out = null;
            }
        }
    }

    @Override public void onInterrupt() {}
}
