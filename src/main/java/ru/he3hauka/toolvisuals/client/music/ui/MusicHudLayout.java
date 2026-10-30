package ru.he3hauka.toolvisuals.client.music.ui;

import ru.he3hauka.toolvisuals.client.music.model.MediaAction;

public record MusicHudLayout(float width,boolean cover,boolean artist,boolean progress,boolean controls) {
    public float contentHeight() { return artist?52:42; }
    public float height() { return contentHeight()+(progress?8:0)+(controls?26:6); }
    public float textX() { return cover?60:10; }
    public float controlsY() { return contentHeight()+(progress?8:0)+1; }
    public float controlX(MediaAction action) {
        return width/2+switch (action) { case PREVIOUS->-44;case TOGGLE->-12;case NEXT->20;default->throw new IllegalArgumentException("Not a HUD transport control"); };
    }
    public MediaAction actionAt(double x,double y) {
        if (!controls || y<controlsY() || y>controlsY()+24) return null;
        for (var action : new MediaAction[]{MediaAction.PREVIOUS,MediaAction.TOGGLE,MediaAction.NEXT})
            if (x>=controlX(action) && x<controlX(action)+24) return action;
        return null;
    }
    public boolean progressAt(double x,double y) { return progress && x>=10 && x<=width-10 && y>=contentHeight()-2 && y<=contentHeight()+7; }
}
