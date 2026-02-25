package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.OutputStream;
import java.util.UUID;

public class BrailleBackService extends AccessibilityService {
    private static final String TAG = "BrailleBackModern";
    // Стандартный UUID для последовательного порта (SPP)
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        connectToDevice();
    }

    private void connectToDevice() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) return;

        // Ищем устройство по именам ELF или Human
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            String name = device.getName();
            if (name != null && (name.contains("ELF") || name.contains("Human"))) {
                Log.i(TAG, "Попытка подключения к: " + name);
                try {
                    socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    socket.connect();
                    outputStream = socket.getOutputStream();
                    Log.i(TAG, "Успешно подключено к дисплею!");
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка подключения: " + e.getMessage());
                }
                break;
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getText() != null && outputStream != null) {
            String text = event.getText().toString();
            try {
                // Отправляем отформатированный текст через драйвер
                outputStream.write(driver.formatText(text));
                outputStream.flush();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка отправки данных на дисплей");
            }
        }
    }

    @Override
    public void onInterrupt() {
        try { if (socket != null) socket.close(); } catch (Exception e) {}
    }
}
