package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Set;

public class BrailleBackService extends AccessibilityService {
    private static final String TAG = "BrailleBackModern";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Сервис запущен");
        connectToDevice();
    }

    private synchronized void connectToDevice() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) return;

        try {
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                if (name != null && (name.contains("Human") || name.contains("ELF"))) {
                    new Thread(() -> {
                        try {
                            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                            socket.connect();
                            outputStream = socket.getOutputStream();
                            
                            // Инициализация HumanWare: ESC + 'T'
                            outputStream.write(new byte[]{0x1B, 0x54}); 
                            outputStream.write(driver.formatText("CONNECTED"));
                            outputStream.flush();
                            Log.i(TAG, "HumanWare подключен!");
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка: " + e.getMessage());
                        }
                    }).start();
                    break;
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Ошибка разрешений Bluetooth");
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (outputStream == null) return;
        
        if (event.getText() != null && !event.getText().isEmpty()) {
            try {
                String text = event.getText().get(0).toString();
                outputStream.write(driver.formatText(text));
                outputStream.flush();
            } catch (Exception e) {
                outputStream = null;
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
