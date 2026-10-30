package ru.he3hauka.toolvisuals.client.gui.surface;

public final class UiPalette {
    public final int panel;
    public final int hudPanel;
    public final int raised;
    public final int hover;
    public final int border;
    public final int text;
    public final int muted;
    public final int faint;
    public final int accent;
    public final int accentText;
    public final int track;
    public final int field;
    public final int inset;
    public final int button;
    public final int selected;
    public final int subtleBorder;

    public UiPalette(float light) {
        panel = mix(0xfc111218, 0xfcf4f5fa, light);
        hudPanel = mix(0xd912131b, 0xeef7f8fc, light);
        raised = mix(0xff1b1c27, 0xffffffff, light);
        hover = mix(0xff292938, 0xffeae5f6, light);
        border = mix(0xff353747, 0xffc2c8d8, light);
        subtleBorder = mix(0xff222431, 0xffe0e3ed, light);
        text = mix(0xfff5f7ff, 0xff202b40, light);
        muted = mix(0xffb1b5c9, 0xff515970, light);
        faint = mix(0xff9198af, 0xff606a82, light);
        accent = mix(0xff9e80f5, 0xff7650ce, light);
        accentText = mix(0xffcbbaf8, 0xff5e389f, light);
        track = mix(0xff414357, 0xffa3aec3, light);
        field = mix(0xff0d0e15, 0xffe9ecf4, light);
        inset = mix(0xff15161f, 0xffeceef6, light);
        button = mix(0xff222431, 0xffe4e7f1, light);
        selected = mix(0xff292438, 0xffe9e0fa, light);
    }

    public static int mix(int from, int to, float amount) {
        float t = Math.max(0, Math.min(1, amount));
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (from >>> shift) & 255;
            int b = (to >>> shift) & 255;
            result |= Math.round(a + (b - a) * t) << shift;
        }
        return result;
    }
}
