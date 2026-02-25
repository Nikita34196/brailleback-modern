package org.google.android.marvin.brailleback;

import android.util.Log;

public class Elf20Driver {
    private static final String TAG = "Elf20Driver";
    
    // Базовая таблица перевода (пока упрощенная)
    // ELF 20 ожидает стандартные ASCII или Dot-коды в зависимости от режима
    public byte[] formatText(String text) {
        if (text == null) return new byte[0];
        
        // Ограничиваем длину текста 20 символами (размер твоего дисплея)
        String truncated = text.length() > 20 ? text.substring(0, 20) : text;
        
        Log.d(TAG, "Подготовка текста для ELF 20: " + truncated);
        return truncated.getBytes(); 
    }

    // Команда для очистки дисплея
    public byte[] getClearCommand() {
        return new byte[]{0x1B, 0x40}; // Стандартный сброс для многих брайлевских терминалов
    }
}
