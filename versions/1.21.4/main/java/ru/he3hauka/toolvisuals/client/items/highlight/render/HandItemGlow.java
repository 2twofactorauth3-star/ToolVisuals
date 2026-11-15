package ru.he3hauka.toolvisuals.client.items.highlight.render;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.compatibility.render.LegacyRenderType;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlights;
import ru.he3hauka.toolvisuals.mixin.itemhighlights.ItemGlowGameAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurChainAccessor;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_HIGHLIGHTS;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class HandItemGlow {
    private static final ResourceLocation EFFECT = ResourceLocation.parse("toolvisuals:item_glow");
    private static final ResourceLocation MASK = ResourceLocation.parse("toolvisuals:item_glow_mask");
    private static final ResourceLocation BLUR = ResourceLocation.parse("toolvisuals:item_glow_blur");
    private static final ThreadLocal<Integer> HAND = ThreadLocal.withInitial(() -> 0);
    private static final class DiscardSource extends MultiBufferSource.BufferSource {
        DiscardSource() { super(new ByteBufferBuilder(1), new LinkedHashMap<>()); }
        @Override public VertexConsumer getBuffer(RenderType type) { return DiscardingVertices.INSTANCE; }
    }
    private static final OutlineBufferSource OUTLINES = new OutlineBufferSource(new DiscardSource());
    private static final LinkedHashMap<RenderType, RenderType> TYPES = new LinkedHashMap<>(64, .75f, true);
    private static TextureTarget mask, blur;
    private static boolean collecting, ready;
    private static long retry;
    private HandItemGlow() {}

    public static int hand(ItemStack item) {
        int previous = HAND.get(); var config = ToolVisualsClient.CONFIG;
        HAND.set(config.active(ITEM_HIGHLIGHTS) && config.flag(ITEM_HIGHLIGHT_HANDS) && !ru.he3hauka.toolvisuals.client.render.filter.NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.POST_EFFECTS) && System.nanoTime() >= retry ? ItemHighlights.color(item) : 0);
        return previous;
    }
    public static void restoreHand(int color) { HAND.set(color); }
    public static void tick(Minecraft client) { if (client.level == null || !ToolVisualsClient.CONFIG.active(ITEM_HIGHLIGHTS) || !ToolVisualsClient.CONFIG.flag(ITEM_HIGHLIGHT_HANDS)) close(); }
    public static void begin() { ready = false; }
    public static void collect(ItemStackRenderState item, PoseStack pose, int light, int overlay) {
        int color = HAND.get(); if (color == 0 || collecting || LegacyGuiGlow.capturing()) return;
        collecting = true;
        try {
            var target = Minecraft.getInstance().getMainRenderTarget(); if (target.width < 1 || target.height < 1) return;
            float factor = Math.min(.5f, 1024f / Math.max(target.width, target.height));
            int width = Math.max(1, Math.round(target.width * factor)), height = Math.max(1, Math.round(target.height * factor));
            if (mask == null || mask.width != width || mask.height != height) {
                close(); mask = LegacyItemTargets.create(width, height, true); blur = LegacyItemTargets.create(width, height, false);
                mask.setClearColor(0, 0, 0, 0);
            }
            try (var state = LegacyItemTargets.bind(mask)) {
                if (!ready) { mask.clear(); mask.bindWrite(true); }
                OUTLINES.setColor(color >> 16 & 255, color >> 8 & 255, color & 255, 255);
                try { item.render(pose, original -> {
                    var outline = original.outline().orElse(null); if (outline == null) return DiscardingVertices.INSTANCE;
                    var type = TYPES.computeIfAbsent(outline, entry -> new LegacyRenderType("toolvisuals_item_mask", entry, () -> mask.bindWrite(true)));
                    if (TYPES.size() > 128) TYPES.remove(TYPES.keySet().iterator().next());
                    return OUTLINES.getBuffer(type);
                }, light, overlay); }
                finally { OUTLINES.endOutlineBatch(); }
                ready = true;
            }
        } catch (RuntimeException error) { failed(error); }
        finally { collecting = false; }
    }
    public static void composite(GameRenderer renderer) {
        if (!ready) return; ready = false;
        try (var state = new LegacyItemTargets()) {
            var client = Minecraft.getInstance();
            var chain = client.getShaderManager().getPostChain(EFFECT, Set.of(PostChain.MAIN_TARGET_ID, MASK, BLUR));
            if (chain == null) { retry = System.nanoTime() + 5_000_000_000L; return; }
            var config = ToolVisualsClient.CONFIG;
            for (var pass : ((MotionBlurChainAccessor)chain).toolvisuals$motionPasses()) {
                var uniform = pass.getShader().getUniform("GlowControl");
                if (uniform != null) uniform.set(config.number(ITEM_HIGHLIGHT_HAND_RADIUS) * 6 * mask.height / 1080f, config.number(ITEM_HIGHLIGHT_HAND_STRENGTH), 0, 0);
            }
            var graph = new FrameGraphBuilder(); var main = client.getMainRenderTarget();
            var targets = new HashMap<ResourceLocation, ResourceHandle<RenderTarget>>();
            targets.put(PostChain.MAIN_TARGET_ID, graph.importExternal("ToolVisuals main", main));
            targets.put(MASK, graph.importExternal("ToolVisuals item mask", mask));
            targets.put(BLUR, graph.importExternal("ToolVisuals item blur", blur));
            chain.addToFrame(graph, main.width, main.height, new PostChain.TargetBundle() {
                @Override public ResourceHandle<RenderTarget> get(ResourceLocation id) { return targets.get(id); }
                @Override public void replace(ResourceLocation id, ResourceHandle<RenderTarget> target) { targets.put(id, target); }
            });
            graph.execute(((ItemGlowGameAccessor)renderer).toolvisuals$itemPool());
        } catch (RuntimeException error) { failed(error); }
    }
    private static void failed(RuntimeException error) { ready = false; retry = System.nanoTime() + 5_000_000_000L; ClientErrors.report("Render held-item glow", error); }
    public static void close() {
        ready = false;
        if (mask != null) { LegacyItemTargets.destroy(mask); mask = null; }
        if (blur != null) { LegacyItemTargets.destroy(blur); blur = null; }
        TYPES.clear();
    }
}
