package ru.he3hauka.toolvisuals.client.world.ambience;

public enum Season {
    WINTER(0xc2d5e6, 0xe0e8ed, 0xd5dfe2, 0xc5d1cf),
    SUMMER(0x509ee9, 0xc4dae7, 0x67b438, 0x3f9335),
    SPRING(0x8cc9ee, 0xd2e3db, 0x94ca55, 0x76bd60),
    AUTUMN(0x9db9cf, 0xd9bfa0, 0xb79c49, 0xd47c25);

    public final int sky, fog, grass, leaves;
    Season(int sky, int fog, int grass, int leaves) {
        this.sky=sky;this.fog=fog;this.grass=grass;this.leaves=leaves;
    }
    public static float intensity(float value) {
        if(!Float.isFinite(value))return 0;
        float clamped=Math.max(0,Math.min(1,value));
        return clamped*(2-clamped);
    }
    public static float atmosphere(float value,float peak,float weight) {
        float daylight=Math.max(0,Math.min(1,peak*255/180));
        return intensity(value)*weight*daylight*daylight;
    }
    public static int blend(int original, int tint, float strength) {
        float amount=Math.max(0,Math.min(1,strength));
        int red=Math.round((original>>16&255)*(1-amount)+(tint>>16&255)*amount);
        int green=Math.round((original>>8&255)*(1-amount)+(tint>>8&255)*amount);
        int blue=Math.round((original&255)*(1-amount)+(tint&255)*amount);
        return original&0xff000000|red<<16|green<<8|blue;
    }
}
