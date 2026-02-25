package org.google.android.marvin.brailleback;

public class Elf20Driver {
    // Константы протокола EuroBraille
    private static final byte ESC = 0x1B;
    private static final byte[] TERMINAL_MODE = {ESC, 0x54}; // Команда перехода в режим терминала

    public byte[] getInitSequence() {
        return TERMINAL_MODE;
    }

    public byte[] formatText(String text) {
        if (text == null) text = "";
        
        // В оригинале используется 8-точечный вывод. 
        // Пока используем стандартную кодировку, чтобы проверить связь.
        byte[] payload = new byte[20];
        byte[] bytes = text.getBytes();
        
        for (int i = 0; i < 20; i++) {
            payload[i] = (i < bytes.length) ? bytes[i] : (byte) 0x20;
        }
        return payload;
    }
}
