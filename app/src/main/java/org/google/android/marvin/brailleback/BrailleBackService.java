package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.hardware.usb.*;
import android.util.Log;
import java.util.HashMap;

public class BrailleBackService extends AccessibilityService {
    private static final String TAG = "BrailleBackModern";
    private UsbDeviceConnection usbConn;
    private UsbEndpoint usbOut;
    private UsbInterface usbInterface;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Служба запущена. Попытка захвата USB...");
        tryConnectUsb();
    }

    private void tryConnectUsb() {
        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        
        for (UsbDevice device : devices.values()) {
            if (manager.hasPermission(device)) {
                // Пытаемся открыть устройство
                UsbDeviceConnection connection = manager.openDevice(device);
                if (connection == null) {
                    Log.e(TAG, "ОШИБКА ПОРТА: Система не дает открыть устройство.");
                    continue;
                }

                // Перебираем интерфейсы, ищем тот, что с Bulk-передачей
                for (int i = 0; i < device.getInterfaceCount(); i++) {
                    UsbInterface intf = device.getInterface(i);
                    
                    // КЛЮЧЕВОЙ МОМЕНТ: claimInterface с параметром true (принудительный захват)
                    if (connection.claimInterface(intf, true)) {
                        for (int j = 0; j < intf.getEndpointCount(); j++) {
                            UsbEndpoint ep = intf.getEndpoint(j);
                            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && 
                                ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                                
                                usbConn = connection;
                                usbInterface = intf;
                                usbOut = ep;
                                
                                Log.i(TAG, "ПОРТ ЗАХВАЧЕН! Шлем сигнал активации.");
                                // Сигнал DTR/RTS (0x03)
                                usbConn.controlTransfer(0x21, 0x22, 0x03, 0, null, 0, 500);
                                
                                byte[] init = driver.getActivationSequence();
                                usbConn.bulkTransfer(usbOut, init, init.length, 500);
                                return;
                            }
                        }
                    }
                }
                connection.close();
            }
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (usbConn != null && usbOut != null && event.getText() != null && !event.getText().isEmpty()) {
            try {
                String text = event.getText().get(0).toString();
                byte[] data = driver.formatText(text);
                usbConn.bulkTransfer(usbOut, data, data.length, 500);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка передачи: " + e.getMessage());
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
