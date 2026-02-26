package org.google.android.marvin.brailleback;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

public class MainActivity extends Activity {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView status = new TextView(this);
        status.setText("Статус: Ожидание подключения дисплея...");
        layout.addView(status);

        // Кнопка 1: Права на Bluetooth (для Android 16)
        Button btnBt = new Button(this);
        btnBt.setText("1. РАЗРЕШИТЬ BLUETOOTH");
        btnBt.setOnClickListener(v -> {
            requestPermissions(new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            }, 1);
            Toast.makeText(this, "Права BT запрошены", Toast.LENGTH_SHORT).show();
        });
        layout.addView(btnBt);

        // Кнопка 2: Активация USB и отправка сигнала DTR
        Button btnUsb = new Button(this);
        btnUsb.setText("2. ПОДКЛЮЧИТЬ USB (DTR SIGNAL)");
        btnUsb.setOnClickListener(v -> {
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            
            if (!deviceList.isEmpty()) {
                UsbDevice device = deviceList.values().iterator().next();
                
                // Сначала запрашиваем системное разрешение
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, 
                        new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                manager.requestPermission(device, pi);
                
                // Пробуем «пнуть» устройство сигналом готовности терминала (DTR)
                try {
                    UsbDeviceConnection connection = manager.openDevice(device);
                    if (connection != null) {
                        // 0x21, 0x22, 0x03 — это стандартная команда активации линий DTR и RTS
                        connection.controlTransfer(0x21, 0x22, 0x03, 0, null, 0, 500);
                        status.setText("Сигнал DTR отправлен! Проверьте дисплей.");
                        connection.close();
                    } else {
                        status.setText("Устройство найдено, но порт заблокирован Android.");
                    }
                } catch (Exception e) {
                    status.setText("Ошибка порта: " + e.getMessage());
                }
            } else {
                status.setText("USB устройство не найдено. Проверьте кабель/OTG.");
            }
        });
        layout.addView(btnUsb);

        // Кнопка 3: Переход в настройки спец. возможностей
        Button btnSettings = new Button(this);
        btnSettings.setText("3. ВКЛЮЧИТЬ СЕРВИС БРАЙЛЯ");
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        layout.addView(btnSettings);

        setContentView(layout);
    }
}
