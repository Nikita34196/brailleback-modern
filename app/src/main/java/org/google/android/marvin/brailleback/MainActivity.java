package org.google.android.marvin.brailleback;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView info = new TextView(this);
        info.setText("Статус USB: Ищу устройства...");
        layout.addView(info);

        Button btnUsb = new Button(this);
        btnUsb.setText("ПРОВЕРИТЬ USB ПОДКЛЮЧЕНИЕ");
        btnUsb.setOnClickListener(v -> {
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            if (deviceList.isEmpty()) {
                info.setText("USB: Ничего не найдено (только зарядка)");
            } else {
                info.setText("USB: Найдено " + deviceList.size() + " устройств(а)");
            }
        });

        layout.addView(btnUsb);
        setContentView(layout);
    }
}
