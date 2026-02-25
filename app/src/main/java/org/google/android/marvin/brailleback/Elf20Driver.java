package org.google.android.marvin.brailleback;

public class Elf20Driver {
    // Команды из оригинального драйвера EuroBraille
    private static final byte ESC = 0x1B;
    private static final byte TERMINAL_MODE = 0x54; // 'T'

    public byte[] getInitSequence() {
        // Последовательность для инициализации терминала: ESC + 'T'
        return new byte[]{ESC, TERMINAL_MODE};
    }

    public byte[] formatText(String text) {
        if (text == null) text = "";
        
        byte[] buffer = new byte[20];
        // В оригинале используется таблица трансляции, 
        // но для теста связи мы передаем ASCII символы
        byte[] bytes = text.getBytes();
        
        for (int i = 0; i < 20; i++) {
            if (i < bytes.length) {
                buffer[i] = bytes[i];
            } else {
                buffer[i] = 0x20; // Пробел
            }
        }
        return buffer;
    }
}
