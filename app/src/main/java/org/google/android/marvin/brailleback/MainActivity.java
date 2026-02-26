package org.google.android.marvin.brailleback;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        Button btn = new Button(this);
        btn.setText("ШАГ 1: РАЗРЕШИТЬ BLUETOOTH");
        btn.setOnClickListener(v -> {
            requestPermissions(new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADMIN
            }, 1);
            Toast.makeText(this, "Запрос отправлен", Toast.LENGTH_SHORT).show();
        });
        
        Button btnSettings = new Button(this);
        btnSettings.setText("ШАГ 2: ОТКРЫТЬ СПЕЦ. ВОЗМОЖНОСТИ");
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        
        layout.addView(btn);
        layout.addView(btnSettings);
        setContentView(layout);
    }
}
