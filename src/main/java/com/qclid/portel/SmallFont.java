package com.qclid.portel;

public class SmallFont {

    public static String toSmallFont(String text) {
        StringBuilder smallFontText = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case 'a':
                    smallFontText.append('ᴀ');
                    break;
                case 'b':
                    smallFontText.append('ʙ');
                    break;
                case 'c':
                    smallFontText.append('ᴄ');
                    break;
                case 'd':
                    smallFontText.append('ᴅ');
                    break;
                case 'e':
                    smallFontText.append('ᴇ');
                    break;
                case 'f':
                    smallFontText.append('ꜰ');
                    break;
                case 'g':
                    smallFontText.append('ɢ');
                    break;
                case 'h':
                    smallFontText.append('ʜ');
                    break;
                case 'i':
                    smallFontText.append('ɪ');
                    break;
                case 'j':
                    smallFontText.append('ᴊ');
                    break;
                case 'k':
                    smallFontText.append('ᴋ');
                    break;
                case 'l':
                    smallFontText.append('ʟ');
                    break;
                case 'm':
                    smallFontText.append('ᴍ');
                    break;
                case 'n':
                    smallFontText.append('ɴ');
                    break;
                case 'o':
                    smallFontText.append('ᴏ');
                    break;
                case 'p':
                    smallFontText.append('ᴘ');
                    break;
                case 'q':
                    smallFontText.append('ǫ');
                    break;
                case 'r':
                    smallFontText.append('ʀ');
                    break;
                case 's':
                    smallFontText.append('ѕ');
                    break;
                case 't':
                    smallFontText.append('ᴛ');
                    break;
                case 'u':
                    smallFontText.append('ᴜ');
                    break;
                case 'v':
                    smallFontText.append('ᴠ');
                    break;
                case 'w':
                    smallFontText.append('ᴡ');
                    break;
                case 'x':
                    smallFontText.append('х');
                    break;
                case 'y':
                    smallFontText.append('ʏ');
                    break;
                case 'z':
                    smallFontText.append('ᴢ');
                    break;
                default:
                    smallFontText.append(c);
                    break;
            }
        }
        return smallFontText.toString();
    }
}
