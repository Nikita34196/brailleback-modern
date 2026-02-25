package org.google.android.marvin.brailleback;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

/**
 * Главная служба BrailleBack для работы с ELF 20
 */
public class BrailleBackService extends AccessibilityService {

    private static final String TAG = "BrailleBackModern";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Служба BrailleBack подключена. Готовность к работе с ELF 20.");
        // Здесь позже добавим инициализацию Bluetooth для EuroBraille
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Получаем текст с экрана
        CharSequence text = event.getText().toString();
        if (text != null && text.length() > 0) {
            Log.d(TAG, "Текст для вывода на дисплей: " + text);
            // Здесь будет отправка текста в драйвер ELF 20
        }
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "Служба прервана");
    }
}
