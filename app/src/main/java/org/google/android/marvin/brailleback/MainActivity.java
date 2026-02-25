package org.google.android.marvin.brailleback;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("BrailleBack Modern активен. Настройте его в Спец. возможностях.");
        setContentView(tv);
    }
}
