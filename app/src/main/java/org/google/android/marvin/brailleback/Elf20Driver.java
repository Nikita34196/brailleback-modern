package org.google.android.marvin.brailleback;

public class Elf20Driver {
    // Команды протокола EuroBraille / HumanWare
    private static final byte ESC = 0x1B;
    private static final byte CMD_TERMINAL = 0x54; // 'T' - вход в режим терминала
    private static final byte CMD_INIT_DISPLAY = 0x49; // 'I' - инициализация дисплея
    private static final byte CMD_SET_WINDOW = 0x57; // 'W' - установка окна вывода

    public byte[] getActivationSequence() {
        // ESC T (режим терминала), затем ESC I (сброс), затем ESC W (окно)
        return new byte[]{ESC, CMD_TERMINAL, ESC, CMD_INIT_DISPLAY, ESC, CMD_SET_WINDOW, 0x01, 0x14}; 
        // 0x01 0x14 — это координаты окна для 20-клеточного дисплея
    }

    public byte[] formatText(String text) {
        if (text == null) text = "";
        byte[] buffer = new byte[20];
        byte[] textBytes = text.getBytes();
        
        for (int i = 0; i < 20; i++) {
            if (i < textBytes.length) {
                buffer[i] = textBytes[i];
            } else {
                buffer[i] = 0x20; // Пробел
            }
        }
        return buffer;
    }
}
