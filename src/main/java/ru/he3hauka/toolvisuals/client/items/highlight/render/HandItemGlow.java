package ru.he3hauka.toolvisuals.client.items.highlight.render;

import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlights;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;
import ru.he3hauka.toolvisuals.mixin.itemhighlights.ItemGlowGameAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurChainAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurPassAccessor;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_HIGHLIGHTS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class HandItemGlow {
    private static final Identifier EFFECT = Identifier.parse("toolvisuals:item_glow");
    private static final Identifier MASK = Identifier.parse("toolvisuals:item_glow_mask");
    private static final Identifier BLOOM = Identifier.parse("toolvisuals:item_glow_bloom");
    private static final Identifier SCRATCH = Identifier.parse("toolvisuals:item_glow_scratch");
    private static final SubmitNodeStorage NODES = new SubmitNodeStorage();
    private static final ThreadLocal<Integer> HAND = ThreadLocal.withInitial(() -> 0);
    private static TextureTarget mask, bloom, scratch;
    private static boolean collecting, preparing, pending, ready;
    private static long retry;
    private HandItemGlow() {}

    public static int hand(ItemStack item) {
        int previous = HAND.get(); var config = ToolVisualsClient.CONFIG;
        HAND.set(config.active(ITEM_HIGHLIGHTS) && config.flag(ITEM_HIGHLIGHT_HANDS) && !NoRender.hides(RenderOption.POST_EFFECTS) && System.nanoTime() >= retry ? ItemHighlights.color(item) : 0);
        return previous;
    }
    public static void restoreHand(int color) { HAND.set(color); }
    public static void tick(Minecraft client) { if (client.level == null || !ToolVisualsClient.CONFIG.active(ITEM_HIGHLIGHTS) || !ToolVisualsClient.CONFIG.flag(ITEM_HIGHLIGHT_HANDS)) close(); }
    public static boolean preparing() { return preparing; }
    public static void begin() { pending = false; ready = false; NODES.getSubmitsPerOrder().clear(); }
    public static void collect(ItemStackRenderState item, PoseStack pose, int light, int overlay) {
        int color = HAND.get(); if (color == 0 || collecting) return;
        collecting = true;
        try { item.submit(pose, NODES, light, overlay, color); pending = true; }
        catch (RuntimeException error) { failed(error); }
        finally { collecting = false; }
    }
    public static void mask(GameRenderer renderer) {
        if (!pending) return;
        var access = (ItemGlowGameAccessor)renderer;
        var target = renderer.mainRenderTarget();
        if (target.width < 1 || target.height < 1) return;
        try {
            float factor = Math.min(.5f, 1024f / Math.max(target.width, target.height));
            int width = Math.max(1, Math.round(target.width * factor)), height = Math.max(1, Math.round(target.height * factor));
            if (mask == null || bloom == null || scratch == null || mask.width != width || mask.height != height) {
                int bloomWidth = Math.max(1, (width + 3) / 4), bloomHeight = Math.max(1, (height + 3) / 4);
                close(); mask = new TextureTarget("ToolVisuals item glow mask", width, height, GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);
                bloom = new TextureTarget("ToolVisuals item glow bloom", bloomWidth, bloomHeight, GpuFormat.RGBA8_UNORM, null);
                scratch = new TextureTarget("ToolVisuals item glow scratch", bloomWidth, bloomHeight, GpuFormat.RGBA8_UNORM, null);
            }
            preparing = true;
            try (var frame = access.toolvisuals$itemFeatures().prepareFrame(NODES)) {
                preparing = false;
                var encoder = RenderSystem.getDevice().createCommandEncoder();
                try (var pass = encoder.createRenderPass(() -> "ToolVisuals item silhouette", mask.getColorTextureView(), Optional.of(new Vector4f()), mask.getDepthTextureView(), OptionalDouble.of(1))) {
                    RenderSystem.bindDefaultUniforms(pass); FeatureRenderDispatcher.renderAllFeatures(pass, frame);
                }
                encoder.submit(); ready = true;
            } finally { preparing = false; NODES.getSubmitsPerOrder().clear(); }
        } catch (RuntimeException error) { failed(error); }
    }
    public static void composite(GameRenderer renderer) {
        if (!ready) return; ready = false;
        var projection = RenderSystem.getProjectionMatrixBuffer(); var type = RenderSystem.getProjectionType();
        try {
            var chain = Minecraft.getInstance().getShaderManager().getPostChain(EFFECT, Set.of(PostChain.MAIN_TARGET_ID, MASK, BLOOM, SCRATCH));
            if (chain == null) { retry = System.nanoTime() + 5_000_000_000L; return; }
            uniforms(chain);
            var frame = new FrameGraphBuilder();
            var targets = new HashMap<Identifier, ResourceHandle<RenderTarget>>();
            targets.put(PostChain.MAIN_TARGET_ID, frame.importExternal("ToolVisuals main", renderer.mainRenderTarget()));
            targets.put(MASK, frame.importExternal("ToolVisuals item mask", mask));
            targets.put(BLOOM, frame.importExternal("ToolVisuals bloom item glow", bloom));
            targets.put(SCRATCH, frame.importExternal("ToolVisuals item scratch", scratch));
            chain.addToFrame(frame, renderer.mainRenderTarget().width, renderer.mainRenderTarget().height, new PostChain.TargetBundle() {
                @Override public ResourceHandle<RenderTarget> get(Identifier id) { return targets.get(id); }
                @Override public void replace(Identifier id, ResourceHandle<RenderTarget> value) { targets.put(id, value); }
            });
            frame.execute(((ItemGlowGameAccessor)renderer).toolvisuals$itemPool());
        } catch (RuntimeException error) { failed(error); }
        finally { RenderSystem.setProjectionMatrix(projection, type); }
    }
    private static void uniforms(PostChain chain) {
        var controls = ItemGlowControls.capture(scratch.height);
        for (var pass : ((MotionBlurChainAccessor)chain).toolvisuals$motionPasses()) {
            var uniforms = ((MotionBlurPassAccessor)pass).toolvisuals$motionUniforms();
            var buffer = uniforms.get("ItemGlowConfig"); if (buffer == null) continue;
            try (var stack = MemoryStack.stackPush()) {
                var bytes = stack.malloc(16); controls.write(bytes);
                if ((buffer.usage() & GpuBuffer.USAGE_COPY_DST) == 0) {
                    var writable = RenderSystem.getDevice().createBuffer(() -> "ToolVisuals item glow controls", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, bytes);
                    uniforms.put("ItemGlowConfig", writable); buffer.close();
                } else {
                    var encoder = RenderSystem.getDevice().createCommandEncoder(); encoder.writeToBuffer(buffer.slice(), bytes); encoder.submit();
                }
            }
        }
    }
    private static void failed(RuntimeException error) { retry = System.nanoTime() + 5_000_000_000L; ready = false; preparing = false; NODES.getSubmitsPerOrder().clear(); ClientErrors.report("Render held-item glow", error); }
    public static void close() {
        ready = false;
        if (mask != null) { mask.destroyBuffers(); mask = null; }
        if (bloom != null) { bloom.destroyBuffers(); bloom = null; }
        if (scratch != null) { scratch.destroyBuffers(); scratch = null; }
    }
}
