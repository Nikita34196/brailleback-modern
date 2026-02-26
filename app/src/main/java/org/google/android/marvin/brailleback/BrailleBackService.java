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
        Log.i(TAG, "Сервис активен. Инициализация USB...");
        tryConnectUsb();
    }

    private void tryConnectUsb() {
        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        
        for (UsbDevice device : devices.values()) {
            if (manager.hasPermission(device)) {
                for (int i = 0; i < device.getInterfaceCount(); i++) {
                    UsbInterface intf = device.getInterface(i);
                    for (int j = 0; j < intf.getEndpointCount(); j++) {
                        UsbEndpoint ep = intf.getEndpoint(j);
                        // Ищем канал для отправки данных (Bulk Out)
                        if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && 
                            ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            
                            usbConn = manager.openDevice(device);
                            if (usbConn != null && usbConn.claimInterface(intf, true)) {
                                usbInterface = intf;
                                usbOut = ep;
                                Log.i(TAG, "USB Подключен к эндпоинту: " + j);
                                
                                // 1. Шлем последовательность активации (Terminal Mode)
                                byte[] activation = driver.getActivationSequence();
                                usbConn.bulkTransfer(usbOut, activation, activation.length, 500);
                                
                                // 2. Шлем приветствие для проверки
                                byte[] welcome = driver.formatText("READY NIKITA");
                                usbConn.bulkTransfer(usbOut, welcome, welcome.length, 500);
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
        if (usbConn != null && usbOut != null) {
            CharSequence text = null;
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED || 
                event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                
                if (event.getContentDescription() != null) {
                    text = event.getContentDescription();
                } else if (event.getText() != null && !event.getText().isEmpty()) {
                    text = event.getText().get(0);
                }
            }

            if (text != null) {
                try {
                    byte[] data = driver.formatText(text.toString());
                    usbConn.bulkTransfer(usbOut, data, data.length, 500);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка отправки: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "Прерывание службы");
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        if (usbConn != null) {
            usbConn.releaseInterface(usbInterface);
            usbConn.close();
        }
        return super.onUnbind(intent);
    }
}
