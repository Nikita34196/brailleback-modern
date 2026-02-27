package org.google.android.marvin.brailleback;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView status = new TextView(this);
        status.setText("Статус Android 16: Ожидание разрешений");
        layout.addView(status);

        Button btnBt = new Button(this);
        btnBt.setText("ШАГ 1: РАЗРЕШИТЬ BLUETOOTH");
        btnBt.setOnClickListener(v -> {
            // Запрос прав именно для Android 16
            requestPermissions(new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            }, 101);
            status.setText("Запрос отправлен. Проверьте всплывающее окно!");
        });
        layout.addView(btnBt);

        Button btnSettings = new Button(this);
        btnSettings.setText("ШАГ 2: ВКЛЮЧИТЬ СЛУЖБУ");
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        layout.addView(btnSettings);

        setContentView(layout);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "БЛЮТУЗ РАЗРЕШЕН!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "ОШИБКА: Сначала включите 'Ограниченные настройки' в меню приложения!", Toast.LENGTH_LONG).show();
        }
    }
}
