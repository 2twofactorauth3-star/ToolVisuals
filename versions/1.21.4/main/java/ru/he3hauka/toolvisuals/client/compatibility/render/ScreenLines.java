package ru.he3hauka.toolvisuals.client.compatibility.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import ru.he3hauka.toolvisuals.client.optimization.geometry.LineMesh;

public final class ScreenLines {
    private ScreenLines() {}
    public static void draw(LineMesh.Snapshot lines,Vec3 camera,GeometryCollector collector){
        var projection=new Matrix4f(VersionCamera.projection()).mul(VersionCamera.view());var inverse=new Matrix4f(projection).invert();var window=Minecraft.getInstance().getWindow();int width=window.getWidth(),height=window.getHeight();if(width<=0||height<=0)return;
        collector.submitCustomGeometry(new PoseStack(),WorldPipelines.TRANSLUCENT,(matrix,vertices)->{
            var a=new Vector4f();var b=new Vector4f();var point=new Vector4f();
            lines.emit((ax,ay,az,bx,by,bz,color,stroke)->{
                a.set((float)(ax-camera.x),(float)(ay-camera.y),(float)(az-camera.z),1);b.set((float)(bx-camera.x),(float)(by-camera.y),(float)(bz-camera.z),1);projection.transform(a);projection.transform(b);
                if(a.w<=.01f&&b.w<=.01f)return;if(a.w<=.01f)a.lerp(b,(.01f-a.w)/(b.w-a.w));else if(b.w<=.01f)b.lerp(a,(.01f-b.w)/(a.w-b.w));
                float dx=(b.x/b.w-a.x/a.w)*width,dy=(b.y/b.w-a.y/a.w)*height,length=(float)Math.hypot(dx,dy);if(length<.001f)return;
                float nx=-dy/length*stroke/width,ny=dx/length*stroke/height;
                emit(matrix,vertices,inverse,point,a,nx,ny,color);emit(matrix,vertices,inverse,point,a,-nx,-ny,color);emit(matrix,vertices,inverse,point,b,-nx,-ny,color);emit(matrix,vertices,inverse,point,b,nx,ny,color);
            });
        });
    }
    private static void emit(PoseStack.Pose matrix,VertexConsumer vertices,Matrix4f inverse,Vector4f point,Vector4f clip,float x,float y,int color){point.set(clip.x+x*clip.w,clip.y+y*clip.w,clip.z,clip.w);inverse.transform(point);float w=point.w;vertices.addVertex(matrix,point.x/w,point.y/w,point.z/w).setColor(color);}
}
