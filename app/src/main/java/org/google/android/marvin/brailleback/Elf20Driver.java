package org.google.android.marvin.brailleback;

public class Elf20Driver {
    private static final byte ESC = 0x1B;

    public byte[] getActivationSequence() {
        // Комбинированная атака: Сброс, Терминал, Инициализация окна
        return new byte[]{
            ESC, (byte)'@', // Сброс устройства
            ESC, (byte)'T', // Режим терминала
            ESC, (byte)'I', // Инициализация
            ESC, (byte)'W', 0x01, 0x14 // Установка окна на 20 клеток
        };
    }

    public byte[] formatText(String text) {
        if (text == null) text = "";
        byte[] buffer = new byte[20];
        byte[] textBytes = text.getBytes();
        for (int i = 0; i < 20; i++) {
            buffer[i] = (i < textBytes.length) ? textBytes[i] : (byte)0x20;
        }
        return buffer;
    }
}
