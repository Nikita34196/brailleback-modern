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
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Сервис подключен");
        try {
            connectToDevice();
        } catch (SecurityException e) {
            Log.e(TAG, "Нет разрешений на Bluetooth!");
        }
    }

    private void connectToDevice() throws SecurityException {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) return;

        for (BluetoothDevice device : adapter.getBondedDevices()) {
            String name = device.getName();
            if (name != null && (name.contains("ELF") || name.contains("Human"))) {
                new Thread(() -> {
                    try {
                        socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                        socket.connect();
                        outputStream = socket.getOutputStream();
                        Log.i(TAG, "Связь с HumanWare установлена!");
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка соединения: " + e.getMessage());
                    }
                }).start();
                break;
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getText() != null && outputStream != null) {
            String text = event.getText().toString();
            try {
                outputStream.write(driver.formatText(text));
                outputStream.flush();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка передачи");
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
