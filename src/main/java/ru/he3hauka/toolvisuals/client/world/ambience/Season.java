package ru.he3hauka.toolvisuals.client.world.ambience;

public enum Season {
    WINTER(0xc6d7e5, 0xdde5e8, 0xa9b7ad, 0xb3b6a7),
    SUMMER(0x72b1ea, 0xc8d9df, 0x70a348, 0x4d873e),
    SPRING(0xa3cbe4, 0xd5e3df, 0x8ebd60, 0x82b668),
    AUTUMN(0xb6bdc7, 0xd6c7b4, 0xa89b58, 0xbc813b);

    public final int sky, fog, grass, leaves;
    Season(int sky, int fog, int grass, int leaves) {
        this.sky=sky;this.fog=fog;this.grass=grass;this.leaves=leaves;
    }
    public static int blend(int original, int tint, float strength) {
        float amount=Math.max(0,Math.min(1,strength));
        int red=Math.round((original>>16&255)*(1-amount)+(tint>>16&255)*amount);
        int green=Math.round((original>>8&255)*(1-amount)+(tint>>8&255)*amount);
        int blue=Math.round((original&255)*(1-amount)+(tint&255)*amount);
        return original&0xff000000|red<<16|green<<8|blue;
    }
}
