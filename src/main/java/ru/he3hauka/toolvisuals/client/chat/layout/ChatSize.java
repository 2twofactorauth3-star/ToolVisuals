package ru.he3hauka.toolvisuals.client.chat.layout;

public record ChatSize(double width,double height) {
    public static final ChatSize DEFAULT=new ChatSize(0,0);
    public ChatSize {width=percent(width);height=percent(height);}
    private static double percent(double value){return Double.isFinite(value)?Math.round(Math.clamp(value,0,100)*10)/10d:0;}
}
