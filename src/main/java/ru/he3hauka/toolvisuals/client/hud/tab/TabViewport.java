package ru.he3hauka.toolvisuals.client.hud.tab;

 
public final class TabViewport {
    public final int total, rows, columns, width, height;
    public final float scale;
    public TabViewport(int total, int width, int height, int columnWidth, int textHeight, float preferredScale) {
        this(total,width,height,columnWidth,textHeight,preferredScale,0);
    }
    public TabViewport(int total, int width, int height, int columnWidth, int textHeight, float preferredScale, int badgeReserve) {
        this.total = Math.max(0, total);
        float maximum = Float.isFinite(preferredScale) && preferredScale > 0 ? preferredScale : 1;
        columns = Math.max(1, (int)(((long)this.total + 19) / 20));
        rows = Math.max(1, (int)(((long)this.total + columns - 1) / columns));
        double tableWidth = (double)columns * Math.max(1, columnWidth) + (columns - 1L) * 5 + 50;
        double tableHeight = (double)rows * 9 + Math.max(0, textHeight) + 36;
        double bestScale = Math.min(maximum, Math.min(Math.max(1, width) / tableWidth, Math.max(1, height-Math.max(0,badgeReserve)) / tableHeight));
         
        scale = bestScale >= maximum ? maximum : Math.nextDown((float)bestScale);
        this.width = Math.max(1, (int)(Math.max(1, width) / scale));
        this.height = Math.max(1, (int)(Math.max(1, height) / scale));
    }
}
