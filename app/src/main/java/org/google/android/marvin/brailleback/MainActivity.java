package org.google.android.marvin.brailleback;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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
        status.setText("Статус: Нажмите кнопку для активации USB");
        layout.addView(status);

        Button btnGrant = new Button(this);
        btnGrant.setText("1. ЗАПРОСИТЬ ДОСТУП К USB");
        btnGrant.setOnClickListener(v -> {
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            
            if (!deviceList.isEmpty()) {
                UsbDevice device = deviceList.values().iterator().next();
                PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                manager.requestPermission(device, permissionIntent);
                status.setText("Запрос отправлен для: " + device.getDeviceName());
            } else {
                status.setText("Ошибка: USB устройство не обнаружено!");
            }
        });

        layout.addView(btnGrant);
        setContentView(layout);
    }
}
