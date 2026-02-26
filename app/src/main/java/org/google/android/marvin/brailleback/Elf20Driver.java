package org.google.android.marvin.brailleback;

public class Elf20Driver {
    // Команды из актуального TalkBack для EuroBraille/HumanWare
    private static final byte ESC = 0x1B;
    private static final byte CMD_TERMINAL = 0x54; // 'T'
    private static final byte CMD_INIT = 0x49;     // 'I'

    public byte[] getInitPacket() {
        // Последовательность: ESC T (Терминал) затем ESC I (Инициализация)
        return new byte[]{ESC, CMD_TERMINAL, ESC, CMD_INIT};
    }

    public byte[] formatText(String text) {
        if (text == null) text = "";
        byte[] cells = new byte[20];
        
        // В современном TalkBack используется Raw-передача символов для EuroBraille
        byte[] textBytes = text.getBytes();
        for (int i = 0; i < 20; i++) {
            if (i < textBytes.length) {
                cells[i] = textBytes[i];
            } else {
                cells[i] = 0x00; // В TalkBack пустая ячейка для этого драйвера — это 0x00
            }
        }
        return cells;
    }
}
