package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.hardware.usb.*;
import android.content.Context;
import android.util.Log;
import java.io.OutputStream;
import java.util.HashMap;

public class BrailleBackService extends AccessibilityService {
    private static final String TAG = "BrailleBackModern";
    private OutputStream outputStream;
    private UsbDeviceConnection usbConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint usbEndpointOut;
    private Elf20Driver driver = new Elf20Driver();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (!tryConnectUsb()) {
            // Если USB не найден, здесь можно вызвать старый метод Bluetooth
            Log.d(TAG, "USB не найден, ожидание подключения...");
        }
    }

    private boolean tryConnectUsb() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        
        for (UsbDevice device : deviceList.values()) {
            // Простая проверка: берем первое попавшееся устройство (для теста)
            usbInterface = device.getInterface(0);
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterface.getEndpoint(i);
                if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && 
                    ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    usbEndpointOut = ep;
                }
            }

            if (usbEndpointOut != null) {
                usbConnection = manager.openDevice(device);
                if (usbConnection != null && usbConnection.claimInterface(usbInterface, true)) {
                    Log.i(TAG, "USB Дисплей подключен!");
                    sendUsbData("READY USB");
                    return true;
                }
            }
        }
        return false;
    }

    private void sendUsbData(String text) {
        if (usbConnection != null && usbEndpointOut != null) {
            byte[] data = driver.formatText(text);
            usbConnection.bulkTransfer(usbEndpointOut, data, data.length, 1000);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getText() != null && !event.getText().isEmpty()) {
            String text = event.getText().get(0).toString();
            sendUsbData(text);
        }
    }

    @Override
    public void onInterrupt() {
        if (usbConnection != null) {
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
        }
    }
}
