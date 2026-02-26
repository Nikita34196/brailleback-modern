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
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private OutputStream btOut;
    private UsbDeviceConnection usbConn;
    private UsbEndpoint usbOut;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Служба TalkBack-Core запущена");
        tryConnectBluetooth();
        tryConnectUsb();
    }

    private void tryConnectBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) return;
        
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            if (device.getName() != null && (device.getName().contains("Human") || device.getName().contains("ELF"))) {
                new Thread(() -> {
                    try {
                        BluetoothSocket s = device.createRfcommSocketToServiceRecord(SPP_UUID);
                        s.connect();
                        btOut = s.getOutputStream();
                        btOut.write(new byte[]{0x1B, 0x54, 0x1B, 0x49}); // Инициализация как в TalkBack
                        btOut.flush();
                        Log.i(TAG, "Bluetooth: OK");
                    } catch (Exception e) { Log.e(TAG, "BT Fail: " + e.getMessage()); }
                }).start();
            }
        }
    }

    private void tryConnectUsb() {
        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
        for (UsbDevice device : manager.getDeviceList().values()) {
            UsbInterface intf = device.getInterface(0);
            usbOut = intf.getEndpoint(0);
            usbConn = manager.openDevice(device);
            if (usbConn != null && usbConn.claimInterface(intf, true)) {
                Log.i(TAG, "USB: OK");
                byte[] init = new byte[]{0x1B, 0x54, 0x1B, 0x49};
                usbConn.bulkTransfer(usbOut, init, init.length, 500);
                return;
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getText() == null || event.getText().isEmpty()) return;
        String text = event.getText().get(0).toString();
        byte[] data = driver.formatText(text);

        try {
            if (btOut != null) { btOut.write(data); btOut.flush(); }
            if (usbConn != null && usbOut != null) usbConn.bulkTransfer(usbOut, data, data.length, 500);
        } catch (Exception e) { Log.e(TAG, "Send Fail"); }
    }

    @Override public void onInterrupt() {}
}
