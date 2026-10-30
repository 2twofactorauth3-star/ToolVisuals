package ru.he3hauka.toolvisuals.client.gui.text;

public final class DisplayRasterState {
    private int width,height,scale;
    private boolean active;
    private boolean invalid=true;
    public boolean update(int width,int height,int scale,boolean active){
        boolean restored=active&&!this.active;this.active=active;
        if(width<=0||height<=0||scale<=0){invalid=true;return false;}
        boolean changed=this.width!=width||this.height!=height||this.scale!=scale;
        this.width=width;this.height=height;this.scale=scale;
        boolean refresh=changed||restored||invalid;invalid=false;return refresh;
    }
}
