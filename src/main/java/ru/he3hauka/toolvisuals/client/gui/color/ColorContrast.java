package ru.he3hauka.toolvisuals.client.gui.color;

import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;

public final class ColorContrast {
    private static final double MINIMUM=4.5;
    private ColorContrast(){}

    public static double ratio(int first,int second){
        double a=luminance(first),b=luminance(second);
        return (Math.max(a,b)+.05)/(Math.min(a,b)+.05);
    }

    public static int readable(int rgb,int background,int fallback){
        int color=0xff000000|rgb;
        if(ratio(color,background)>=MINIMUM)return color;
        int target=0xff000000|fallback;
        if(ratio(target,background)<MINIMUM)target=ratio(0xff000000,background)>ratio(0xffffffff,background)?0xff000000:0xffffffff;
        float low=0,high=1;
        for(int step=0;step<10;step++){
            float amount=(low+high)/2;
            if(ratio(UiPalette.mix(color,target,amount),background)>=MINIMUM)high=amount;else low=amount;
        }
        return UiPalette.mix(color,target,high);
    }

    private static double luminance(int color){return channel(color>>16&255)*.2126+channel(color>>8&255)*.7152+channel(color&255)*.0722;}
    private static double channel(int value){double s=value/255d;return s<=.04045?s/12.92:Math.pow((s+.055)/1.055,2.4);}
}
