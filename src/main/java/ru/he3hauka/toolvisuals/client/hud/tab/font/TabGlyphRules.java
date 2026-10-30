package ru.he3hauka.toolvisuals.client.hud.tab.font;

public final class TabGlyphRules {
    private TabGlyphRules() {}
    public static boolean smoothText(int codePoint){if(codePoint>=0x20&&codePoint<=0x7e||Character.isLetterOrDigit(codePoint))return true;int type=Character.getType(codePoint);return type==Character.NON_SPACING_MARK||type==Character.COMBINING_SPACING_MARK;}
}
