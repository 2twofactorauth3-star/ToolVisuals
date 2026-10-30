package ru.he3hauka.toolvisuals.client.world;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.effects.EffectBudget;
import ru.he3hauka.toolvisuals.client.effects.WorldEffects;
import ru.he3hauka.toolvisuals.client.effects.render.EffectRenderer;
import ru.he3hauka.toolvisuals.client.hud.target.VisualTarget;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.geometry.LineMesh;
import ru.he3hauka.toolvisuals.client.optimization.geometry.UnitCircle;
import ru.he3hauka.toolvisuals.client.optimization.scene.NearbyEntities;
import ru.he3hauka.toolvisuals.client.projectile.ProjectilePrediction;
import ru.he3hauka.toolvisuals.client.render.CustomHitboxes;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.waypoint.Waypoints;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class VisualWorld {
    private static final int SEGMENTS = 64;
    private static final List<Bubble> BUBBLES = new ArrayList<>();
    private static volatile Frame frame = new Frame(LineMesh.Snapshot.EMPTY, null);
    private static ClientLevel level;
    private static final Vec3 RIGHT = new Vec3(1, 0, 0), FORWARD = new Vec3(0, 0, 1);
    private VisualWorld() {}

    private static final LineMesh STROKES=new LineMesh();
    private static final class Hat {
        public final Vec3 center;
        public final float radius, height;
        public final int color;
        Hat(Vec3 center, float radius, float height, int color) { this.center = center; this.radius = radius; this.height = height; this.color = color; }
    }
    private static final class Frame {
        public final LineMesh.Snapshot strokes;
        public final Hat hat;
        Frame(LineMesh.Snapshot strokes, Hat hat) { this.strokes = strokes; this.hat = hat; }
    }
    private static final class Bubble {
        public final Vec3 center;
        public final long started;
        public final int duration, color;
        public final float radius;
        Bubble(Vec3 center, long started, int duration, int color, float radius) {
            this.center = center; this.started = started; this.duration = duration; this.color = color; this.radius = radius;
        }
    }

    public static void tick(Minecraft client) {
        if (level != client.level || client.player == null) {
            level = client.level; BUBBLES.clear(); frame = new Frame(LineMesh.Snapshot.EMPTY, null);
        }
        if (level != null) BUBBLES.removeIf(bubble -> level.getGameTime() - bubble.started > bubble.duration);
        if (!ToolVisualsClient.CONFIG.active(HIT_BUBBLES)) BUBBLES.clear();
        int limit = EffectBudget.limit(OPT_MAX_BUBBLES, 24);
        while (BUBBLES.size() > limit) BUBBLES.removeFirst();
    }

    public static void hit(LivingEntity entity) {
        var config = ToolVisualsClient.CONFIG;
        if (!config.active(HIT_BUBBLES) || level == null || entity.level() != level) return;
        if (BUBBLES.size() >= EffectBudget.limit(OPT_MAX_BUBBLES, 24)) BUBBLES.removeFirst();
        BUBBLES.add(new Bubble(entity.position().add(0, entity.getBbHeight() * .55, 0), level.getGameTime(),
                (int)config.value(BUBBLE_DURATION), config.color(BUBBLE_COLOR), config.number(BUBBLE_RADIUS)));
    }

     
    public static void extract(LevelRenderState state) {
        long started=Diagnostics.start(Diagnostics.Section.WORLD_EXTRACT);
        try{extractFrame(state);}finally{Diagnostics.end(Diagnostics.Section.WORLD_EXTRACT,started);}
    }
    private static void extractFrame(LevelRenderState state) {
        ru.he3hauka.toolvisuals.client.waypoint.WorldLabels.extract(state);
        WorldEffects.extract(state);
        var client = Minecraft.getInstance();
        var config = ToolVisualsClient.CONFIG;
        if (client.player == null || client.level == null || client.level != level) {
            ru.he3hauka.toolvisuals.client.render.blockoverlay.BlockOverlay.clear();
            frame = new Frame(LineMesh.Snapshot.EMPTY, null); return;
        }
        float partial = state.worldPartialTicks;
        double time = ru.he3hauka.toolvisuals.client.animation.EffectTime.at(state.gameTime, partial);
        var strokes=STROKES;strokes.clear();
        ru.he3hauka.toolvisuals.client.render.blockoverlay.BlockOverlay.extract(strokes);
        var gps=ru.he3hauka.toolvisuals.client.navigation.gps.Gps.visible(client);
        if(gps!=null) {
            var position=new Vec3(gps.x(),gps.y(),gps.z());
            line(strokes,position,position.add(0,3,0),0xc0bda5ff,2);
            ring(strokes,position.add(0,.025,0),RIGHT,FORWARD,.4,0xbda5ff,.85f,1.5f);
        }
        if(config.flag(PRESENTS_ROUTE)) {
            var presents=ru.he3hauka.toolvisuals.client.presents.ToolPresents.route();
            for(int index=0;index<Math.min(4,presents.size());index++) {
                var point=presents.get(index);
                var position=new Vec3(point.x()+.5,point.y()+.05,point.z()+.5);
                ring(strokes,position,RIGHT,FORWARD,.65,0xbda5ff,index==0?.95f:.45f,index==0?2:1.25f);
                line(strokes,position,position.add(0,index==0?2:1,0),index==0?0xd0bda5ff:0x708aa2b8,1.5f);
                if(index>0) {
                    var previous=presents.get(index-1);
                    line(strokes,new Vec3(previous.x()+.5,previous.y()+.1,previous.z()+.5),position.add(0,.05,0),0x608aa2b8,1.5f);
                }
            }
        }
        for(var path:ProjectilePrediction.paths(partial)) {
            int color=0xe0000000|config.color(PREDICT_COLOR);float width=config.number(PREDICT_WIDTH);
            for(int i=1;i<path.points.size();i++)line(strokes,path.points.get(i-1),path.points.get(i),color,width);
            if(path.hit!=null&&config.flag(PREDICT_LANDING)) {
                Vec3 right=RIGHT,up=FORWARD;
                if(path.hit instanceof net.minecraft.world.phys.BlockHitResult hit) {
                    var axis=hit.getDirection().getAxis();
                    if(axis==net.minecraft.core.Direction.Axis.X){right=new Vec3(0,1,0);up=FORWARD;}
                    else if(axis==net.minecraft.core.Direction.Axis.Z){right=RIGHT;up=new Vec3(0,1,0);}
                }
                var center=path.hit.getLocation();
                if(path.hit instanceof net.minecraft.world.phys.BlockHitResult hit)center=center.add(Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i()).scale(.015));
                ring(strokes,center,right,up,.22,color&0xffffff,.9f,width);
            }
        }
        if(config.active(WAYPOINTS)&&config.flag(WAYPOINT_BEAM)) {
            double range=config.value(WAYPOINT_RANGE);
            for(var point:ToolVisualsClient.WAYPOINTS.entries(Waypoints.context())) {
                var pos=new Vec3(point.x,point.y,point.z);
                if(!point.enabled||!point.dimension.equals(Waypoints.dimension())||point.death&&!config.flag(WAYPOINT_DEATH)
                        ||pos.distanceToSqr(client.player.position())>range*range)continue;
                int pointColor=point.style.sample(ru.he3hauka.toolvisuals.client.config.color.ColorStyle.clock(),0);
                line(strokes,pos,pos.add(0,3,0),0xb0000000|pointColor,2);
                ring(strokes,pos.add(0,.025,0),RIGHT,FORWARD,.35,pointColor,.8f,1.5f);
            }
        }
        if (config.active(CUSTOM_HITBOXES)) {
            var parts=CustomHitboxes.parts(config);
            var colors=new java.util.EnumMap<CustomHitboxes.Part,Integer>(CustomHitboxes.Part.class);
            for(var part:parts)colors.put(part,Math.round(config.number(HITBOX_OPACITY)*255)<<24|config.color(part.color));
            float width = config.number(HITBOX_LINE_WIDTH);
            int count = 0;
            for (var entity : NearbyEntities.get(client)) {
                if(parts.isEmpty())break;
                if (!CustomHitboxes.selected(entity) || entity.distanceToSqr(client.player) > 4096) continue;
                var bounds=entity.getBoundingBox().move(entity.getPosition(partial).subtract(entity.position()));
                CustomHitboxes.emit(bounds,entity.getEyePosition(partial),entity.getViewVector(partial),parts,
                        (part,ax,ay,az,bx,by,bz)->strokes.line(ax,ay,az,bx,by,bz,colors.get(part),width));
                if (++count >= 256) break;
            }
        }
        var target = VisualTarget.current();
        if (config.active(TARGET_ESP) && target != null && !NoRender.hidesEntity(target)) {
            Vec3 pos = target.getPosition(partial);
            double radius = target.getBbWidth() * .65 + .12;
            double h = target.getBbHeight();
            double phase = time * .1 * config.number(TARGET_SPEED);
            float width = config.number(TARGET_LINE_WIDTH);
            int color = config.color(TARGET_COLOR);
            ring(strokes, pos.add(0, h * (.5 + Math.sin(phase) * .35), 0), RIGHT, FORWARD, radius, color, .95f, width);
            if (config.value(TARGET_STYLE) == 1)
                ring(strokes, pos.add(0, h * (.5 - Math.sin(phase) * .35), 0), RIGHT, FORWARD, radius * 1.12, color, .6f, width);
        }
        if (config.active(HIT_BUBBLES)) {
            var right = new Vector3f(1, 0, 0).rotate(state.cameraRenderState.orientation);
            var up = new Vector3f(0, 1, 0).rotate(state.cameraRenderState.orientation);
            Vec3 ringRight = new Vec3(right), ringUp = new Vec3(up);
            for (Bubble bubble : BUBBLES) {
                float t = (float)Math.clamp((time - bubble.started) / bubble.duration, 0, 1);
                ring(strokes, bubble.center.add(0, t * .35, 0), ringRight, ringUp,
                        bubble.radius * (.2 + t * .8), config.color(BUBBLE_COLOR), (1 - t) * .9f, 2);
            }
        }
        Hat hat = null;
        if (config.active(CHINA_HAT) && !state.cameraRenderState.isFirstPerson && !client.player.isInvisible()
                && !NoRender.hidesEntity(client.player)) {
            Vec3 center = client.player.getPosition(partial).add(0, client.player.getBbHeight() + .08, 0);
            int color = Math.round(config.number(HAT_OPACITY) * 255) << 24 | config.color(HAT_COLOR);
            hat = new Hat(center, config.number(HAT_RADIUS), config.number(HAT_HEIGHT), color);
            ring(strokes, center, RIGHT, FORWARD, hat.radius, color & 0xffffff, .85f, 1.5f);
        }
        frame = new Frame(strokes.snapshot(), hat);
    }

    private static void ring(LineMesh strokes, Vec3 center, Vec3 right, Vec3 up, double radius, int color, float opacity, float width) {
        if (opacity <= .01f) return;
        int segments = EffectBudget.segments(center.distanceToSqr(Minecraft.getInstance().player.position()), SEGMENTS);
        boolean fast = Optimizations.enabled(OPT_FAST_VISUALS);
        int glow = Math.round(opacity * .16f * 255) << 24 | color;
        int main = Math.round(opacity * 255) << 24 | color;
        double px=center.x+right.x*radius,py=center.y+right.y*radius,pz=center.z+right.z*radius;
        for(int i=1;i<=segments;i++){
            double angle=i*Math.PI*2/segments;
            double x=(fast?UnitCircle.x(i,segments):Math.cos(angle))*radius,z=(fast?UnitCircle.z(i,segments):Math.sin(angle))*radius;
            double nx=center.x+right.x*x+up.x*z,ny=center.y+right.y*x+up.y*z,nz=center.z+right.z*x+up.z*z;
            strokes.line(px,py,pz,nx,ny,nz,glow,width*3);strokes.line(px,py,pz,nx,ny,nz,main,width);
            px=nx;py=ny;pz=nz;
        }
    }
    private static void line(LineMesh strokes,Vec3 from,Vec3 to,int color,float width){strokes.line(from.x,from.y,from.z,to.x,to.y,to.z,color,width);}

    public static void submit(LevelRenderState state, SubmitNodeCollector collector) {
        ru.he3hauka.toolvisuals.client.render.blockoverlay.BlockOverlay.submit(state,collector);
        EffectRenderer.submit(collector);
        Frame current = frame;
        Vec3 camera = state.cameraRenderState.pos;
        var pose = new PoseStack();
        if(current.strokes.size>0)collector.submitCustomGeometry(pose,RenderTypes.linesTranslucentNoDepthWrite(),(matrix,vertices)->{
            long started=Diagnostics.start(Diagnostics.Section.WORLD_SUBMIT);
            try{current.strokes.emit((ax,ay,az,bx,by,bz,color,width)->{
                double dx=bx-ax,dy=by-ay,dz=bz-az,length=Math.sqrt(dx*dx+dy*dy+dz*dz);
                float nx=length<1e-4?0:(float)(dx/length),ny=length<1e-4?0:(float)(dy/length),nz=length<1e-4?0:(float)(dz/length);
                vertex(vertices,matrix,ax,ay,az,camera,nx,ny,nz,color,width);vertex(vertices,matrix,bx,by,bz,camera,nx,ny,nz,color,width);
            });}finally{Diagnostics.end(Diagnostics.Section.WORLD_SUBMIT,started);}
        });
        Hat hat = current.hat;
        boolean fast = Optimizations.enabled(OPT_FAST_VISUALS);
        if (hat != null) collector.submitCustomGeometry(pose, RenderTypes.debugTriangleFan(), (matrix, vertices) -> {
            Vec3 center = hat.center.subtract(camera);
            vertices.addVertex(matrix, (float)center.x, (float)center.y + hat.height, (float)center.z).setColor(hat.color);
            for (int i = 0; i <= SEGMENTS; i++) {
                double angle = i * Math.PI * 2 / SEGMENTS;
                vertices.addVertex(matrix, (float)(center.x + (fast ? UnitCircle.x(i, SEGMENTS) : Math.cos(angle)) * hat.radius), (float)center.y,
                        (float)(center.z - (fast ? UnitCircle.z(i, SEGMENTS) : Math.sin(angle)) * hat.radius)).setColor(hat.color);
            }
        });
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, double x,double y,double z, Vec3 camera,
                               float nx, float ny, float nz, int color, float width) {
        vertices.addVertex(pose, (float)(x - camera.x), (float)(y - camera.y), (float)(z - camera.z)).setColor(color)
                .setNormal(pose, nx, ny, nz).setLineWidth(width);
    }
}
