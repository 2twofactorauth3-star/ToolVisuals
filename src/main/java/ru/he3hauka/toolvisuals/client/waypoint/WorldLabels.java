package ru.he3hauka.toolvisuals.client.waypoint;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.camera.FreeLook;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.pvp.TotemCounter;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class WorldLabels {
    private static final String[] DIRECTIONS={"→","↘","↓","↙","←","↖","↑","↗"};
    private static final class Label {
        public final float x,y,scale;
        public final String text;
        public final int color;
        Label(float x,float y,float scale,String text,int color){this.x=x;this.y=y;this.scale=scale;this.text=text;this.color=color;}
    }
    private static volatile List<Label> labels=List.of();
    private WorldLabels() {}
    public static void extract(LevelRenderState state) {
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;var next=new ArrayList<Label>();
        if(client.player==null||client.level==null){labels=List.of();return;}
        var camera=state.cameraRenderState;
        if(camera.projectionMatrix==null||camera.viewRotationMatrix==null){labels=List.of();return;}
        for(var marker:ru.he3hauka.toolvisuals.client.navigation.gps.Gps.visibleMarkers(client)) {
            var gps=marker.target();
            var position=new Vec3(gps.x(),gps.y()+1,gps.z());
            String name=marker.name()+" · "+Math.round(client.player.position().distanceTo(position))+" m";
            add(next,camera,position,name,0xbda5ff,1,true);
        }
        if(config.active(WAYPOINTS)) {
            double range=config.value(WAYPOINT_RANGE);
            for(var point:ToolVisualsClient.WAYPOINTS.entries(Waypoints.context())) {
                if(!point.enabled||!point.dimension.equals(Waypoints.dimension())||point.death&&!config.flag(WAYPOINT_DEATH))continue;
                var position=new Vec3(point.x,point.y+1,point.z);double distance=client.player.position().distanceTo(position);
                if(distance>range)continue;
                String name=point.death?UiI18n.tr("toolvisuals.waypoints.death").getString():point.name;
                if(config.flag(WAYPOINT_DISTANCE))name+=" · "+Math.round(distance)+" m";
                add(next,camera,position,name,point.style.sample(ru.he3hauka.toolvisuals.client.config.color.ColorStyle.clock(),0),config.number(WAYPOINT_SCALE));
            }
        }
        if(config.flag(PRESENTS_MARKERS)) {
            var route=ru.he3hauka.toolvisuals.client.presents.ToolPresents.route();
            for(int index=0;index<Math.min(3,route.size());index++) {
                var point=route.get(index);
                var position=new Vec3(point.x()+.5,point.y()+1.5,point.z()+.5);
                String name=UiI18n.tr("toolvisuals.presents.marker",index+1,point.id(),Math.round(client.player.position().distanceTo(position))).getString();
                add(next,camera,position,name,index==0?0xbda5ff:0x8aa2b8,index==0?1:.8f);
            }
        }
        if(config.active(TOTEM_POP_COUNTER)&&config.flag(TOTEM_LABELS)&&!NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.NAME_TAGS)) {
            for(var entity:ru.he3hauka.toolvisuals.client.optimization.scene.NearbyEntities.get(client)) {
                if(!(entity instanceof Player)||entity.isRemoved()||entity.level()!=client.level)continue;
                int count=TotemCounter.count(entity.getUUID());
                if(count<=0||entity.distanceToSqr(client.player)>4096||entity.isInvisibleTo(client.player)||NoRender.hidesEntity(entity)
                        ||entity==client.player&&(camera.isFirstPerson||!config.flag(TOTEM_SELF)||FreeLook.active()&&!FreeLook.showsOwnLabels(entity))
                        ||entity!=client.player&&(!config.flag(TOTEM_OTHERS)||!client.player.hasLineOfSight(entity)))continue;
                add(next,camera,entity.getPosition(state.worldPartialTicks).add(0,entity.getBbHeight()+.65,0),
                        UiI18n.tr("toolvisuals.totem.label",count).getString(),ToolVisualsClient.CONFIG.color(TOTEM_COLOR),.9f);
            }
        }
        labels=List.copyOf(next);
    }
    private static void add(List<Label> target,CameraRenderState camera,Vec3 world,String text,int color,float scale) {
        add(target,camera,world,text,color,scale,false);
    }
    private static void add(List<Label> target,CameraRenderState camera,Vec3 world,String text,int color,float scale,boolean edge) {
        var relative=world.subtract(camera.pos);var clip=new Vector4f((float)relative.x,(float)relative.y,(float)relative.z,1);
        camera.viewRotationMatrix.transform(clip);camera.projectionMatrix.transform(clip);
        if(!Float.isFinite(clip.x)||!Float.isFinite(clip.y)||!Float.isFinite(clip.w)||!edge&&clip.w<=.01)return;
        float divisor=edge?Math.max(.01f,Math.abs(clip.w)):clip.w;
        float x=clip.x/divisor,y=clip.y/divisor;
        float extent=Math.max(Math.abs(x),Math.abs(y));
        if(edge&&(clip.w<=.01||extent>.88f)) {
            if(extent<.01f){y=-1;extent=1;}
            x*=.88f/extent;y*=.88f/extent;
            int direction=Math.floorMod((int)Math.round(Math.atan2(-y,x)*4/Math.PI),8);
            text=DIRECTIONS[direction]+" "+text;
        } else if(!edge&&extent>1.1)return;
        target.add(new Label(x*.5f+.5f,.5f-y*.5f,scale,text,0xff000000|color));
    }
    public static void draw(GuiGraphicsExtractor g,UiPalette palette) {
        var client=Minecraft.getInstance();float density=Math.clamp(client.getWindow().getHeight()/720f,1,2)/client.getWindow().getGuiScale();
        g.pose().pushMatrix();g.pose().scale(density,density);
        for(var label:labels) {
            float scale=.8f*label.scale,w=UiPaint.textWidth(g,label.text,scale)+14,h=22*label.scale;
            float x=label.x*g.guiWidth()/density-w/2,y=label.y*g.guiHeight()/density-h;
            x=Math.clamp(x,2,Math.max(2,g.guiWidth()/density-w-2));y=Math.clamp(y,2,Math.max(2,g.guiHeight()/density-h-2));
            UiPaint.rounded(g,x,y,w,h,5,palette.hudPanel);
            UiPaint.rounded(g,x,y+5,2,h-10,1,label.color);
            UiPaint.centered(g,label.text,x+3,y,w-3,h,scale,label.color,1);
        }
        g.pose().popMatrix();
    }
}
