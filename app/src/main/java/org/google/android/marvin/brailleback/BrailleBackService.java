package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.bluetooth.*;
import android.hardware.usb.*;
import android.util.Log;
import java.io.OutputStream;
import java.util.UUID;

public class BrailleBackService extends AccessibilityService {
    private static final String TAG = "BrailleBackModern";
    private OutputStream bluetoothOut;
    private UsbDeviceConnection usbConn;
    private UsbEndpoint usbOut;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Служба ожила. Ищу дисплей...");
        tryConnectAll();
    }

    private void tryConnectAll() {
        // Пробуем USB
        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
        if (!usbManager.getDeviceList().isEmpty()) {
            for (UsbDevice device : usbManager.getDeviceList().values()) {
                usbConn = usbManager.openDevice(device);
                if (usbConn != null) {
                    UsbInterface intf = device.getInterface(0);
                    usbOut = intf.getEndpoint(0); // Обычно первый на выход
                    usbConn.claimInterface(intf, true);
                    Log.i(TAG, "USB Подключен!");
                    return;
                }
            }
        }
        
        // Если USB нет, пробуем Bluetooth
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            for (BluetoothDevice dev : adapter.getBondedDevices()) {
                if (dev.getName().contains("Human") || dev.getName().contains("ELF")) {
                    new Thread(() -> {
                        try {
                            BluetoothSocket s = dev.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                            s.connect();
                            bluetoothOut = s.getOutputStream();
                            Log.i(TAG, "Bluetooth Подключен!");
                        } catch (Exception e) { Log.e(TAG, "BT Error"); }
                    }).start();
                }
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String text = (event.getText() != null && !event.getText().isEmpty()) 
                      ? event.getText().get(0).toString() : "";
        if (text.isEmpty()) return;

        byte[] data = driver.formatText(text);
        
        // Шлем везде, где есть связь
        try {
            if (usbConn != null && usbOut != null) usbConn.bulkTransfer(usbOut, data, data.length, 500);
            if (bluetoothOut != null) { bluetoothOut.write(data); bluetoothOut.flush(); }
        } catch (Exception e) { Log.e(TAG, "Send Error"); }
    }

    @Override public void onInterrupt() {}
}
