package ru.he3hauka.toolvisuals.client.menu;

import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;

public final class TitleMenuLayout {
    public static final class Box {
        public final int x,y,width,height;
        public Box(int x,int y,int width,int height) { this.x=x;this.y=y;this.width=width;this.height=height; }
    }
    public final float scale;
    public final int width,height;
    public final boolean compact;
    public final Box panel,singleplayer,multiplayer,accounts,versions,settings,realms,quit,switchMenu,theme,language;
    public TitleMenuLayout(int guiWidth,int guiHeight,int gameScale) {
        var viewport=new UiViewport(guiWidth,guiHeight,gameScale);
        scale=Math.min(viewport.scale,Math.min(guiWidth/400f,guiHeight/350f));
        width=Math.round(guiWidth/scale);height=Math.round(guiHeight/scale);compact=height<480;
        int panelWidth=Math.min(360,width-32),panelHeight=compact?276:328;
        int x=(width-panelWidth)/2,y=Math.max(12,(height-50-panelHeight)/2),padding=compact?18:22;
        panel=new Box(x,y,panelWidth,panelHeight);
        int left=x+padding,full=panelWidth-2*padding,half=(full-8)/2;
        int row=y+(compact?76:90),primary=compact?34:42;
        singleplayer=new Box(left,row,full,primary);row+=primary+(compact?6:8);
        multiplayer=new Box(left,row,full,primary);row+=primary+(compact?10:12);
        int cards=compact?28:32;
        accounts=new Box(left,row,half,cards);versions=new Box(left+half+8,row,full-half-8,cards);row+=cards+(compact?8:10);
        int secondary=compact?28:34;
        settings=new Box(left,row,half,secondary);realms=new Box(left+half+8,row,full-half-8,secondary);row+=secondary+(compact?6:8);
        quit=new Box(left,row,full,secondary);
        switchMenu=new Box(14,height-38,116,26);
        language=new Box(width-114,height-38,50,26);theme=new Box(width-56,height-38,42,26);
    }
}
