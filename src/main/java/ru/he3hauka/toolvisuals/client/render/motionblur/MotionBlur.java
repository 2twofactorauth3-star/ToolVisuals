package ru.he3hauka.toolvisuals.client.render.motionblur;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurGameAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurPassAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurChainAccessor;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.MOTION_BLUR;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.MOTION_BLUR_STRENGTH;

public final class MotionBlur {
    public static final Identifier EFFECT = Identifier.parse("toolvisuals:motion_blur");
    private static final MotionHistory HISTORY = new MotionHistory();
    private MotionBlur() {}

    public static boolean active() {
        var client = Minecraft.getInstance();
        return ToolVisualsClient.CONFIG.active(MOTION_BLUR) && ToolVisualsClient.CONFIG.value(MOTION_BLUR_STRENGTH)>0 && client.level != null && client.player != null
                && client.gui.screen() == null && client.gui.overlay() == null && !client.getWindow().isIconified();
    }

    public static void requested(java.util.List<Identifier> effects) {
        if (active() && !effects.contains(EFFECT)) effects.add(EFFECT);
        else if (!active()) HISTORY.clear();
    }

    public static void prepare(GameRenderer renderer) {
        if (!active()) return;
        var client = Minecraft.getInstance(); var camera = renderer.gameRenderState().levelRenderState.cameraRenderState;
        for (var chain : ((MotionBlurGameAccessor)renderer).toolvisuals$motionEffects()) {
            if (!EFFECT.equals(chain.id())) continue;
            var sample = HISTORY.sample(chain, client.level, renderer.mainRenderTarget().width, renderer.mainRenderTarget().height,
                    camera.pos.x,camera.pos.y,camera.pos.z,camera.projectionMatrix,camera.viewRotationMatrix,
                    ToolVisualsClient.CONFIG.value(MOTION_BLUR_STRENGTH),System.nanoTime());
            boolean zeroToOne=RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();
            for (var pass : ((MotionBlurChainAccessor)chain).toolvisuals$motionPasses()) {
                var uniforms = ((MotionBlurPassAccessor)pass).toolvisuals$motionUniforms();
                var buffer = uniforms.get("MotionConfig");
                if (buffer == null) continue;
                try (var stack = MemoryStack.stackPush()) {
                    var bytes=stack.malloc(80);
                    sample.clipToPrevious().get(0,bytes);bytes.position(64);
                    bytes.putFloat(sample.exposureScale()).putFloat(sample.maximumPixels()).putFloat(zeroToOne?1:2).putFloat(zeroToOne?0:-1).flip();
                    if ((buffer.usage() & GpuBuffer.USAGE_COPY_DST) == 0) {
                        var writable = RenderSystem.getDevice().createBuffer(() -> "ToolVisuals motion blur", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, bytes);
                        uniforms.put("MotionConfig", writable);
                        buffer.close();
                    } else {
                        var encoder = RenderSystem.getDevice().createCommandEncoder();
                        encoder.writeToBuffer(buffer.slice(), bytes);
                        encoder.submit();
                    }
                }
            }
        }
    }
}
