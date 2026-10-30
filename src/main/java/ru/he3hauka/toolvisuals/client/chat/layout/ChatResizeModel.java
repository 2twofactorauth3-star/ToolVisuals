package ru.he3hauka.toolvisuals.client.chat.layout;

public final class ChatResizeModel {
    public enum Edge {
        NONE(false,false),TOP(false,true),RIGHT(true,false),CORNER(true,true);
        public final boolean width,height;
        Edge(boolean width,boolean height){this.width=width;this.height=height;}
    }
    public record Box(double left,double top,double right,double bottom){}
    public record Size(double width,double height){}
    private Edge edge=Edge.NONE;
    private Box origin;
    private double grabX,grabY,width,height;
    private boolean changed;
    public static Edge pick(Box box,double x,double y){
        boolean top=y>=box.top-5&&y<=box.top+2&&x>=box.left&&x<=box.right+5;
        boolean right=x>=box.right-2&&x<=box.right+5&&y>=box.top-5&&y<=box.bottom;
        return top&&right?Edge.CORNER:top?Edge.TOP:right?Edge.RIGHT:Edge.NONE;
    }
    public boolean begin(Box box,double x,double y){
        edge=pick(box,x,y);if(edge==Edge.NONE)return false;
        origin=box;grabX=box.right-x;grabY=box.top-y;width=box.right-box.left;height=box.bottom-box.top;changed=false;return true;
    }
    public boolean move(double x,double y,double maximumWidth,double maximumHeight){
        if(!dragging())return false;
        double nextWidth=edge.width?Math.clamp(x+grabX-origin.left,Math.min(80,maximumWidth),maximumWidth):width;
        double nextHeight=edge.height?Math.clamp(origin.bottom-y-grabY,Math.min(36,maximumHeight),maximumHeight):height;
        changed|=Math.abs(nextWidth-width)>=.01||Math.abs(nextHeight-height)>=.01;width=nextWidth;height=nextHeight;return true;
    }
    public boolean dragging(){return edge!=Edge.NONE;}
    public Edge edge(){return edge;}
    public Size size(){return new Size(width,height);}
    public Size finish(){var result=dragging()&&changed?size():null;edge=Edge.NONE;origin=null;changed=false;return result;}
}
