package org.google.android.marvin.brailleback;

public class Elf20Driver {
    public byte[] formatText(String text) {
        if (text == null) text = "";
        // Очистка текста от лишних символов
        text = text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
        
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
