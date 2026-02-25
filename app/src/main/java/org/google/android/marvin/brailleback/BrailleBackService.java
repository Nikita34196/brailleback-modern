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
    // Стандартный UUID для последовательного порта (SPP)
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Служба спец. возможностей подключена");
        connectToDevice();
    }

    private synchronized void connectToDevice() {
        if (outputStream != null) return; // Уже подключено

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Log.e(TAG, "Bluetooth не активен");
            return;
        }

        try {
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                if (name != null && (name.contains("ELF") || name.contains("Human"))) {
                    Log.i(TAG, "Найдено подходящее устройство: " + name);
                    new Thread(() -> {
                        try {
                            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                            socket.connect();
                            outputStream = socket.getOutputStream();
                            Log.i(TAG, "Успешное соединение с " + name);
                            
                            // Отправляем приветственный сигнал на дисплей
                            outputStream.write(driver.formatText("READY"));
                            outputStream.flush();
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка при установке связи: " + e.getMessage());
                            socket = null;
                            outputStream = null;
                        }
                    }).start();
                    break;
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Android 16: Ошибка прав доступа к Bluetooth");
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Если текст изменился, а связи нет — пробуем восстановить
        if (outputStream == null) {
            connectToDevice();
        }

        if (outputStream != null && event.getText() != null && !event.getText().isEmpty()) {
            try {
                String text = event.getText().get(0).toString();
                byte[] data = driver.formatText(text);
                outputStream.write(data);
                outputStream.flush();
            } catch (Exception e) {
                Log.e(TAG, "Сбой отправки данных, сброс потока");
                outputStream = null;
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "Служба прервана");
        closeConnection();
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        closeConnection();
        return super.onUnbind(intent);
    }

    private void closeConnection() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при закрытии сокета");
        }
        outputStream = null;
        socket = null;
    }
}
