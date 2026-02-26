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
        Log.i(TAG, "Служба активирована");
        tryConnectUsb();
    }

    private void tryConnectUsb() {
        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        
        for (UsbDevice device : devices.values()) {
            if (manager.hasPermission(device)) {
                usbInterface = device.getInterface(0);
                usbConn = manager.openDevice(device);
                
                if (usbConn != null && usbConn.claimInterface(usbInterface, true)) {
                    // МАГИЧЕСКАЯ КОМАНДА: Активация DTR/RTS (сигнал готовности для дисплея)
                    usbConn.controlTransfer(0x21, 0x22, 0x03, 0, null, 0, 500);
                    
                    // Ищем Bulk Out эндпоинт
                    for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                        UsbEndpoint ep = usbInterface.getEndpoint(j);
                        if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && 
                            ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            usbOut = ep;
                            break;
                        }
                    }

                    if (usbOut != null) {
                        Log.i(TAG, "Связь установлена. Шлем инициализацию.");
                        byte[] init = driver.getActivationSequence();
                        usbConn.bulkTransfer(usbOut, init, init.length, 500);
                        
                        // Тестовое сообщение
                        byte[] test = driver.formatText("CONNECTED");
                        usbConn.bulkTransfer(usbOut, test, test.length, 500);
                    }
                }
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
            } catch (Exception e) { Log.e(TAG, "Ошибка: " + e.getMessage()); }
        }
    }

    @Override public void onInterrupt() {}
}
