package ru.he3hauka.toolvisuals.client.hand;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.ToDoubleFunction;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

public final class HandPose {
    public final float x,y,z,scale,rotationX,rotationY,rotationZ,stretchX,stretchY,stretchZ;
    public final boolean hidden,noEquip,noSwing;
    private final Matrix4f rotation;

    public HandPose(ToDoubleFunction<VisualSetting> values,HandSide side) {
        x=read(values,side,HandProperty.X);y=read(values,side,HandProperty.Y);z=read(values,side,HandProperty.Z);
        scale=read(values,side,HandProperty.SCALE);
        rotationX=read(values,side,HandProperty.ROTATION_X);rotationY=read(values,side,HandProperty.ROTATION_Y);rotationZ=read(values,side,HandProperty.ROTATION_Z);
        stretchX=read(values,side,HandProperty.STRETCH_X);stretchY=read(values,side,HandProperty.STRETCH_Y);stretchZ=read(values,side,HandProperty.STRETCH_Z);
        hidden=read(values,side,HandProperty.HIDE)>.5;noEquip=read(values,side,HandProperty.NO_EQUIP)>.5;noSwing=read(values,side,HandProperty.NO_SWING)>.5;
        rotation=new Matrix4f().rotation(new Quaternionf().rotationXYZ((float)Math.toRadians(rotationX),(float)Math.toRadians(rotationY),(float)Math.toRadians(rotationZ)));
    }

    private static float read(ToDoubleFunction<VisualSetting> values,HandSide side,HandProperty property) {
        var setting=property.setting(side);return (float)setting.sanitize(values.applyAsDouble(setting));
    }

    public void offset(PoseStack pose) {
        pose.translate(x,y,z);
    }

    public void shape(PoseStack pose,AABB bounds) {
        float pivotX=(float)((bounds.minX+bounds.maxX)*.5);
        float pivotY=(float)((bounds.minY+bounds.maxY)*.5);
        float pivotZ=(float)((bounds.minZ+bounds.maxZ)*.5);
        pose.translate(pivotX,pivotY,pivotZ);
        if(rotationX!=0||rotationY!=0||rotationZ!=0)pose.mulPose(rotation);
        pose.scale(scale*stretchX,scale*stretchY,scale*stretchZ);
        pose.translate(-pivotX,-pivotY,-pivotZ);
    }
}
