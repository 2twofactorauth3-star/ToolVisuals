package ru.he3hauka.toolvisuals.client.animation.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.animation.Emote;
import ru.he3hauka.toolvisuals.client.animation.EmotePose;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;

public final class EmoteGlyph {
    private EmoteGlyph(){}
    public static void draw(GuiGraphicsExtractor g,Emote emote,float x,float y,int color) {
        var pose=EmotePose.sample(emote,emote.duration*.38);float sway=emote==Emote.SPIN?12:0;
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().rotate((float)Math.toRadians(sway));
        UiPaint.rounded(g,-4,-20,8,8,4,color);line(g,0,-10,0,5,color);
        limb(g,-5,-8,pose.rightArm,13,color);limb(g,5,-8,pose.leftArm,13,color);
        limb(g,-3,5,pose.rightLeg,14,color);limb(g,3,5,pose.leftLeg,14,color);g.pose().popMatrix();
    }
    private static void limb(GuiGraphicsExtractor g,float x,float y,EmotePose.Rotation rotation,float length,int color) {
        float depth=(float)Math.sin(rotation.x())*length;
        float dx=-(float)Math.sin(rotation.z())*length*.9f+(float)Math.sin(rotation.y())*depth*.7f;
        float dy=(float)Math.cos(rotation.x())*(float)Math.cos(rotation.z())*length-depth*.3f;
        line(g,x,y,x+dx,y+dy,color);
    }
    private static void line(GuiGraphicsExtractor g,float x,float y,float endX,float endY,int color) {
        float dx=endX-x,dy=endY-y,length=(float)Math.hypot(dx,dy);
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().rotate((float)Math.atan2(dy,dx));UiPaint.rounded(g,0,-1.5f,length,3,1.5f,color);g.pose().popMatrix();
    }
}
