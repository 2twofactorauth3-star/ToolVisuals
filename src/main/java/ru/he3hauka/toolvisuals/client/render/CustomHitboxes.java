package ru.he3hauka.toolvisuals.client.render;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class CustomHitboxes {
    public enum Part {
        BOTTOM(HITBOX_COLOR),SIDES(HITBOX_COLOR),TOP(HITBOX_COLOR),EYES(HITBOX_EYE_COLOR),DIRECTION(HITBOX_DIRECTION_COLOR);
        public final VisualSetting color;
        Part(VisualSetting color) { this.color=color; }
    }
    public static final class Line {
        public final Vec3 from,to;
        public final Part part;
        public Line(Vec3 from,Vec3 to,Part part) { this.from=from;this.to=to;this.part=part; }
    }
    private CustomHitboxes() {}

    public static boolean selected(Entity entity) {
        var client = Minecraft.getInstance();
        if (!(entity instanceof LivingEntity living) || !living.isAlive()
                || entity.isRemoved() || entity.isSpectator() || client.player == null
                || entity.isInvisibleTo(client.player) || NoRender.hidesEntity(entity)) return false;
        if(entity==client.player&&(!ToolVisualsClient.CONFIG.flag(HITBOX_SELF)||client.options.getCameraType().isFirstPerson()))return false;
        return ToolVisualsClient.CONFIG.flag(entity instanceof Player ? HITBOX_PLAYERS : HITBOX_MOBS);
    }

    public static Set<Part> parts(ClientConfig config) {
        var parts=EnumSet.noneOf(Part.class);
        if(config.flag(HITBOX_OUTLINE)) {
            if(config.flag(HITBOX_BOTTOM))parts.add(Part.BOTTOM);
            if(config.flag(HITBOX_SIDES))parts.add(Part.SIDES);
            if(config.flag(HITBOX_TOP))parts.add(Part.TOP);
        }
        if(config.flag(HITBOX_EYES))parts.add(Part.EYES);
        if(config.flag(HITBOX_DIRECTION))parts.add(Part.DIRECTION);
        return parts;
    }
    @FunctionalInterface public interface LineSink {void line(Part part,double ax,double ay,double az,double bx,double by,double bz);}
    public static List<Line> geometry(AABB bounds,Vec3 eye,Vec3 direction,Set<Part> parts) {
        var lines=new ArrayList<Line>(17);
        emit(bounds,eye,direction,parts,(part,ax,ay,az,bx,by,bz)->lines.add(new Line(new Vec3(ax,ay,az),new Vec3(bx,by,bz),part)));
        return List.copyOf(lines);
    }
    public static void emit(AABB bounds,Vec3 eye,Vec3 direction,Set<Part> parts,LineSink sink){
        if(parts.isEmpty())return;
        for(int corner=0;corner<8;corner++)for(int axis=1;axis<=4;axis<<=1)if((corner&axis)==0){
            Part part=axis==2?Part.SIDES:(corner&2)==0?Part.BOTTOM:Part.TOP;
            if(parts.contains(part))edge(bounds,corner,corner|axis,part,sink);
        }
        if(parts.contains(Part.EYES)){
            sink.line(Part.EYES,bounds.minX,eye.y,bounds.minZ,bounds.maxX,eye.y,bounds.minZ);
            sink.line(Part.EYES,bounds.maxX,eye.y,bounds.minZ,bounds.maxX,eye.y,bounds.maxZ);
            sink.line(Part.EYES,bounds.maxX,eye.y,bounds.maxZ,bounds.minX,eye.y,bounds.maxZ);
            sink.line(Part.EYES,bounds.minX,eye.y,bounds.maxZ,bounds.minX,eye.y,bounds.minZ);
        }
        if(parts.contains(Part.DIRECTION)&&direction.lengthSqr()>1e-8){double factor=2/Math.sqrt(direction.lengthSqr());sink.line(Part.DIRECTION,eye.x,eye.y,eye.z,eye.x+direction.x*factor,eye.y+direction.y*factor,eye.z+direction.z*factor);}
    }
    private static void edge(AABB bounds,int a,int b,Part part,LineSink sink){
        sink.line(part,(a&1)==0?bounds.minX:bounds.maxX,(a&2)==0?bounds.minY:bounds.maxY,(a&4)==0?bounds.minZ:bounds.maxZ,
                (b&1)==0?bounds.minX:bounds.maxX,(b&2)==0?bounds.minY:bounds.maxY,(b&4)==0?bounds.minZ:bounds.maxZ);
    }
}
