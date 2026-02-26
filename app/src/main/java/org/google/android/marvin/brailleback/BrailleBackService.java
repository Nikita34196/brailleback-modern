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
        Log.i(TAG, "Служба запущена. Поиск разрешенного USB...");
        tryConnectUsb();
    }

    private void tryConnectUsb() {
        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        
        for (UsbDevice device : devices.values()) {
            if (manager.hasPermission(device)) {
                // Перебираем все интерфейсы устройства
                for (int i = 0; i < device.getInterfaceCount(); i++) {
                    UsbInterface intf = device.getInterface(i);
                    // Ищем подходящий эндпоинт для вывода данных
                    for (int j = 0; j < intf.getEndpointCount(); j++) {
                        UsbEndpoint ep = intf.getEndpoint(j);
                        if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && 
                            ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            
                            usbConn = manager.openDevice(device);
                            if (usbConn != null && usbConn.claimInterface(intf, true)) {
                                usbInterface = intf;
                                usbOut = ep;
                                Log.i(TAG, "USB ПОДКЛЮЧЕН: Эндпоинт " + j);
                                
                                // Инициализация HumanWare (ESC T ESC I)
                                byte[] init = new byte[]{0x1B, 0x54, 0x1B, 0x49};
                                usbConn.bulkTransfer(usbOut, init, init.length, 500);
                                return;
                            }
                        }
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
                int result = usbConn.bulkTransfer(usbOut, data, data.length, 500);
                if (result < 0) Log.e(TAG, "Ошибка передачи USB");
            } catch (Exception e) {
                Log.e(TAG, "Сбой: " + e.getMessage());
            }
        }
    }

    @Override
    public void onInterrupt() {
        if (usbConn != null) {
            usbConn.releaseInterface(usbInterface);
            usbConn.close();
        }
    }
}
