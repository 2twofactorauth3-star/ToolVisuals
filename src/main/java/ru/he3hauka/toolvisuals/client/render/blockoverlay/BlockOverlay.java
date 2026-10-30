package ru.he3hauka.toolvisuals.client.render.blockoverlay;

import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.effects.render.EffectPipeline;
import ru.he3hauka.toolvisuals.client.optimization.geometry.LineMesh;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.BLOCK_HIGHLIGHT;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class BlockOverlay {
    private record Frame(List<AABB> boxes, int color) {}
    private static volatile Frame frame = new Frame(List.of(), 0);
    private BlockOverlay() {}

    public static boolean active() {
        var client = Minecraft.getInstance();
        return ToolVisualsClient.CONFIG.active(BLOCK_HIGHLIGHT) && !ReplayScope.active() && client.level != null && client.player != null
                && client.gui.screen() == null && !NoRender.hides(RenderOption.BLOCK_OUTLINE)
                && client.hitResult instanceof BlockHitResult hit && !hit.isInside()
                &&hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK
                && client.player.isWithinBlockInteractionRange(hit.getBlockPos(), 0) && !client.level.getBlockState(hit.getBlockPos()).isAir();
    }

    public static void clear() { frame = new Frame(List.of(), 0); }

    public static void extract(LineMesh lines) {
        if (!active()) { clear(); return; }
        var client = Minecraft.getInstance(); var config = ToolVisualsClient.CONFIG;
        var position = ((BlockHitResult)client.hitResult).getBlockPos();
        var shape = config.flag(BLOCK_HIGHLIGHT_FULL_CUBE) ? Shapes.block()
                : client.level.getBlockState(position).getShape(client.level, position, CollisionContext.of(client.player));
        int color = config.color(BLOCK_HIGHLIGHT_COLOR);
        if (config.flag(BLOCK_HIGHLIGHT_OUTLINE)) shape.forAllEdges((ax, ay, az, bx, by, bz) -> lines.line(
                position.getX() + ax, position.getY() + ay, position.getZ() + az,
                position.getX() + bx, position.getY() + by, position.getZ() + bz, 0xe6000000 | color, config.number(BLOCK_HIGHLIGHT_WIDTH)));
        var boxes = config.flag(BLOCK_HIGHLIGHT_FILL) ? shape.toAabbs().stream()
                .limit(128).map(box -> box.move(position).inflate(.001)).toList() : List.<AABB>of();
        frame = new Frame(boxes, Math.round(config.number(BLOCK_HIGHLIGHT_OPACITY) * 255) << 24 | color);
    }

    public static void submit(LevelRenderState state, SubmitNodeCollector collector) {
        var current = frame;
        if (current.boxes.isEmpty()) return;
        var camera = state.cameraRenderState.pos;
        collector.submitCustomGeometry(new PoseStack(), EffectPipeline.TYPE, (pose, vertices) -> {
            for (var box : current.boxes) {
                float x = (float)(box.minX - camera.x), y = (float)(box.minY - camera.y), z = (float)(box.minZ - camera.z);
                float X = (float)(box.maxX - camera.x), Y = (float)(box.maxY - camera.y), Z = (float)(box.maxZ - camera.z);
                quad(vertices, pose, current.color, x,y,z, X,y,z, X,Y,z, x,Y,z);
                quad(vertices, pose, current.color, x,y,Z, x,Y,Z, X,Y,Z, X,y,Z);
                quad(vertices, pose, current.color, x,y,z, x,Y,z, x,Y,Z, x,y,Z);
                quad(vertices, pose, current.color, X,y,z, X,y,Z, X,Y,Z, X,Y,z);
                quad(vertices, pose, current.color, x,y,z, x,y,Z, X,y,Z, X,y,z);
                quad(vertices, pose, current.color, x,Y,z, X,Y,z, X,Y,Z, x,Y,Z);
            }
        });
    }

    private static void quad(VertexConsumer vertices, PoseStack.Pose pose, int color,
                             float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz) {
        vertices.addVertex(pose,ax,ay,az).setColor(color);vertices.addVertex(pose,bx,by,bz).setColor(color);
        vertices.addVertex(pose,cx,cy,cz).setColor(color);vertices.addVertex(pose,dx,dy,dz).setColor(color);
    }
}
