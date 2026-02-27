package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.bluetooth.*;
import android.hardware.usb.*;
import android.util.Log;
import java.io.OutputStream;
import java.util.UUID;
import java.util.Set;

public class BrailleBackService extends AccessibilityService {
    private static final String TAG = "BrailleBackModern";
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private OutputStream btOut;
    private UsbDeviceConnection usbConn;
    private UsbEndpoint usbOut;
    private UsbInterface usbInterface;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Служба запущена. Начинаю поиск устройств...");
        // Запускаем бесконечный цикл поиска в отдельном потоке
        new Thread(this::connectionLoop).start();
    }

    private void connectionLoop() {
        while (true) {
            if (btOut == null && usbOut == null) {
                tryConnectUsb();
                tryConnectBluetooth();
            }
            try { Thread.sleep(5000); } catch (InterruptedException e) { break; }
        }
    }

    private void tryConnectBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) return;

        try {
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            for (BluetoothDevice device : devices) {
                String name = device.getName();
                if (name != null && (name.contains("Human") || name.contains("ELF"))) {
                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    socket.connect();
                    btOut = socket.getOutputStream();
                    btOut.write(driver.getActivationSequence());
                    btOut.flush();
                    Log.i(TAG, "Bluetooth: Подключено к " + name);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "BT Connect Error: " + e.getMessage());
        }
    }

    private void tryConnectUsb() {
        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
        for (UsbDevice device : manager.getDeviceList().values()) {
            if (manager.hasPermission(device)) {
                usbConn = manager.openDevice(device);
                if (usbConn != null) {
                    // Активация линий передачи данных (DTR/RTS)
                    usbConn.controlTransfer(0x21, 0x22, 0x03, 0, null, 0, 500);
                    
                    usbInterface = device.getInterface(0);
                    usbConn.claimInterface(usbInterface, true);
                    
                    for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                        UsbEndpoint ep = usbInterface.getEndpoint(i);
                        if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && 
                            ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            usbOut = ep;
                            break;
                        }
                    }

                    if (usbOut != null) {
                        byte[] init = driver.getActivationSequence();
                        usbConn.bulkTransfer(usbOut, init, init.length, 500);
                        Log.i(TAG, "USB: Подключено!");
                    }
                }
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getText() == null || event.getText().isEmpty()) return;
        String text = event.getText().get(0).toString();
        byte[] data = driver.formatText(text);

        try {
            if (btOut != null) {
                btOut.write(data);
                btOut.flush();
            }
            if (usbConn != null && usbOut != null) {
                usbConn.bulkTransfer(usbOut, data, data.length, 500);
            }
        } catch (Exception e) {
            btOut = null; // Сброс при ошибке для переподключения
            Log.e(TAG, "Ошибка передачи данных");
        }
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (btOut != null) btOut.close();
            if (usbConn != null) {
                usbConn.releaseInterface(usbInterface);
                usbConn.close();
            }
        } catch (Exception ignored) {}
    }
}
