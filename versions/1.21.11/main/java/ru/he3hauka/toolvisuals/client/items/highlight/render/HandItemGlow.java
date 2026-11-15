package ru.he3hauka.toolvisuals.client.items.highlight.render;

import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.buffers.GpuBuffer;
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
import ru.he3hauka.toolvisuals.mixin.itemhighlights.ItemGlowGameAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurChainAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurPassAccessor;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_HIGHLIGHTS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class HandItemGlow {
    private static final Identifier EFFECT = Identifier.parse("toolvisuals:item_glow");
    private static final Identifier MASK = Identifier.parse("toolvisuals:item_glow_mask");
    private static final Identifier BLUR = Identifier.parse("toolvisuals:item_glow_blur");
    private static final SubmitNodeStorage NODES = new SubmitNodeStorage();
    private static final ThreadLocal<Integer> HAND = ThreadLocal.withInitial(() -> 0);
    private static TextureTarget mask, blur;
    private static FeatureRenderDispatcher features;
    private static OutlineBufferSource outlines;
    private static final class DiscardSource extends MultiBufferSource.BufferSource {
        DiscardSource() { super(new ByteBufferBuilder(1), new java.util.LinkedHashMap<>()); }
        @Override public VertexConsumer getBuffer(RenderType type) { return DiscardingVertices.INSTANCE; }
    }
    private static boolean collecting, preparing, pending, ready;
    private static long retry;
    private HandItemGlow() {}

    public static int hand(ItemStack item) {
        int previous = HAND.get(); var config = ToolVisualsClient.CONFIG;
        HAND.set(config.active(ITEM_HIGHLIGHTS) && config.flag(ITEM_HIGHLIGHT_HANDS) && !ru.he3hauka.toolvisuals.client.render.filter.NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.POST_EFFECTS) && System.nanoTime() >= retry ? ItemHighlights.color(item) : 0);
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
        var client = Minecraft.getInstance(); var target = client.getMainRenderTarget();
        if (target.width < 1 || target.height < 1) return;
        var oldColor = RenderSystem.outputColorTextureOverride; var oldDepth = RenderSystem.outputDepthTextureOverride;
        try {
            float factor = Math.min(.5f, 1024f / Math.max(target.width, target.height));
            int width = Math.max(1, Math.round(target.width * factor)), height = Math.max(1, Math.round(target.height * factor));
            if (mask == null || mask.width != width || mask.height != height) {
                close(); mask = new TextureTarget("ToolVisuals item glow mask", width, height, true);
                blur = new TextureTarget("ToolVisuals item glow blur", width, height, false);
            }
            if (features == null) {
                var discard = new DiscardSource(); outlines = new OutlineBufferSource();
                features = new FeatureRenderDispatcher(NODES, client.getBlockRenderer(), discard, client.getAtlasManager(), outlines, discard, client.font);
            }
            var encoder = RenderSystem.getDevice().createCommandEncoder();
            encoder.clearColorAndDepthTextures(mask.getColorTexture(), 0, mask.getDepthTexture(), 1);
            RenderSystem.outputColorTextureOverride = mask.getColorTextureView();
            RenderSystem.outputDepthTextureOverride = mask.getDepthTextureView();
            try { features.renderAllFeatures(); }
            finally { outlines.endOutlineBatch(); }
            ready = true;
        } catch (RuntimeException error) { failed(error); }
        finally {
            try { if (features != null) features.endFrame(); NODES.clear(); }
            finally { RenderSystem.outputColorTextureOverride = oldColor; RenderSystem.outputDepthTextureOverride = oldDepth; }
        }
    }
    public static void composite(GameRenderer renderer) {
        if (!ready) return; ready = false;
        var projection = RenderSystem.getProjectionMatrixBuffer(); var type = RenderSystem.getProjectionType();
        try {
            var chain = Minecraft.getInstance().getShaderManager().getPostChain(EFFECT, Set.of(PostChain.MAIN_TARGET_ID, MASK, BLUR));
            if (chain == null) { retry = System.nanoTime() + 5_000_000_000L; return; }
            uniforms(chain);
            var frame = new FrameGraphBuilder();
            var targets = new HashMap<Identifier, ResourceHandle<RenderTarget>>();
            targets.put(PostChain.MAIN_TARGET_ID, frame.importExternal("ToolVisuals main", Minecraft.getInstance().getMainRenderTarget()));
            targets.put(MASK, frame.importExternal("ToolVisuals item mask", mask));
            targets.put(BLUR, frame.importExternal("ToolVisuals item blur", blur));
            chain.addToFrame(frame, Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, new PostChain.TargetBundle() {
                @Override public ResourceHandle<RenderTarget> get(Identifier id) { return targets.get(id); }
                @Override public void replace(Identifier id, ResourceHandle<RenderTarget> value) { targets.put(id, value); }
            });
            frame.execute(((ItemGlowGameAccessor)renderer).toolvisuals$itemPool());
        } catch (RuntimeException error) { failed(error); }
        finally { RenderSystem.setProjectionMatrix(projection, type); }
    }
    private static void uniforms(PostChain chain) {
        var config = ToolVisualsClient.CONFIG;
        float radius = config.number(ITEM_HIGHLIGHT_RADIUS) * 6 * mask.height / 1080f;
        for (var pass : ((MotionBlurChainAccessor)chain).toolvisuals$motionPasses()) {
            var uniforms = ((MotionBlurPassAccessor)pass).toolvisuals$motionUniforms();
            var buffer = uniforms.get("ItemGlowConfig"); if (buffer == null) continue;
            try (var stack = MemoryStack.stackPush()) {
                var bytes = stack.malloc(16); bytes.putFloat(radius).putFloat(config.number(ITEM_HIGHLIGHT_STRENGTH)).putFloat(0).putFloat(0).flip();
                if ((buffer.usage() & GpuBuffer.USAGE_COPY_DST) == 0) {
                    var writable = RenderSystem.getDevice().createBuffer(() -> "ToolVisuals item glow controls", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, bytes);
                    uniforms.put("ItemGlowConfig", writable); buffer.close();
                } else {
                    var encoder = RenderSystem.getDevice().createCommandEncoder(); encoder.writeToBuffer(buffer.slice(), bytes);
                }
            }
        }
    }
    private static void failed(RuntimeException error) { retry = System.nanoTime() + 5_000_000_000L; ready = false; preparing = false; NODES.getSubmitsPerOrder().clear(); ClientErrors.report("Render held-item glow", error); }
    public static void close() {
        ready = false;
        if (mask != null) { mask.destroyBuffers(); mask = null; }
        if (blur != null) { blur.destroyBuffers(); blur = null; }
    }
}
