package ru.he3hauka.toolvisuals.client.menu;

import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;

public final class TitleBrand {
    public static final String TEXT="ToolVisuals";
    private TitleBrand() {}

    public static int color(double position,double seconds,boolean light) {
        float x=(float)Math.clamp(position,0,1);
        int white=light?0xff263044:0xfff0edff;
        int violet=light?0xff7042ac:0xffb59bff;
        int ice=light?0xff306a88:0xff9edfee;
        int orchid=light?0xff914a91:0xffe4a5e0;
        int base=x<.42f?UiPalette.mix(white,violet,smooth(.30f,.42f,x)):
                x<.74f?UiPalette.mix(violet,ice,smooth(.42f,.74f,x)):UiPalette.mix(ice,orchid,smooth(.74f,1,x));
        double center=(seconds%6)/3-.5,distance=(x-center)/.105;
        float shine=(float)Math.exp(-distance*distance/2)*.72f;
        return UiPalette.mix(base,light?0xff854eb0:0xfffcf9ff,shine);
    }

    private static float smooth(float start,float end,float value) {
        float t=Math.clamp((value-start)/(end-start),0,1);
        return t*t*(3-2*t);
    }
}
