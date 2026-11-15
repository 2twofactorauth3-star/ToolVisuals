package ru.he3hauka.toolvisuals.client.items.highlight.render;

import java.util.LinkedHashMap;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import ru.he3hauka.toolvisuals.client.compatibility.render.LegacyRenderType;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.items.highlight.ItemHighlights;
import ru.he3hauka.toolvisuals.mixin.itemhighlights.ItemGlowGuiAccessor;

public final class LegacyGuiGlow {
    private record Key(Item item, DataComponentPatch components, int count, int seed) {}
    private static final class Icon {
        final TextureTarget target = LegacyItemTargets.create(32, 32, true);
        long updated;
    }
    private static final LinkedHashMap<Key, Icon> ICONS = new LinkedHashMap<>(64, .75f, true);
    private static final class IconBuffers extends MultiBufferSource.BufferSource {
        private final LinkedHashMap<RenderType, RenderType> types = new LinkedHashMap<>(64, .75f, true);
        private TextureTarget target;
        IconBuffers() { super(new ByteBufferBuilder(65536), new LinkedHashMap<>()); }
        private RenderType maskType(RenderType original) {
            var type = types.computeIfAbsent(original, entry -> new LegacyRenderType("toolvisuals_gui_item_mask", entry, () -> target.bindWrite(true)));
            if (types.size() > 128) types.remove(types.keySet().iterator().next());
            return type;
        }
        @Override public VertexConsumer getBuffer(RenderType type) { return super.getBuffer(maskType(type)); }
    }
    private static final IconBuffers BUFFERS = new IconBuffers();
    private static boolean capturing;
    private static int budget;
    private static long retry;
    private LegacyGuiGlow() {}
    public static boolean capturing() { return capturing; }
    public static void beginFrame() { budget = 2; }
    public static void clear() { for (var icon : ICONS.values()) LegacyItemTargets.destroy(icon.target); ICONS.clear(); BUFFERS.types.clear(); }
    public static void draw(GuiGraphics graphics, LivingEntity owner, Level level, ItemStack stack, int x, int y, int seed, ItemHighlights.GuiStyle style) {
        if (style.color() >>> 24 == 0 || capturing || System.nanoTime() < retry) return;
        graphics.flush();
        try {
            var key = new Key(stack.getItem(), stack.getComponentsPatch(), stack.getCount(), seed);
            var icon = ICONS.get(key);
            if (budget > 0 && (icon == null || System.nanoTime() - icon.updated > 250_000_000L)) {
                budget--;
                if (icon == null) {
                    if (ICONS.size() >= 64) { var first = ICONS.entrySet().iterator(); var old = first.next(); LegacyItemTargets.destroy(old.getValue().target); first.remove(); }
                    icon = new Icon(); ICONS.put(key, icon);
                }
                capture(icon, owner, level, stack, seed);
            }
            if (icon == null || icon.updated == 0) return;
            int padding = (int)Math.ceil(style.radius() + 1); float uv = padding / 32f;
            var vertices = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
            var pose = graphics.pose().last().pose();
            vertex(vertices, pose, x - 8 - padding, y - 8 - padding, -uv, 1 + uv, style);
            vertex(vertices, pose, x - 8 - padding, y + 24 + padding, -uv, -uv, style);
            vertex(vertices, pose, x + 24 + padding, y + 24 + padding, 1 + uv, -uv, style);
            vertex(vertices, pose, x + 24 + padding, y - 8 - padding, 1 + uv, 1 + uv, style);
            ItemGlowPipeline.gui(icon.target.getColorTextureId()).draw(vertices.buildOrThrow());
        } catch (RuntimeException error) { retry = System.nanoTime() + 5_000_000_000L; ClientErrors.report("Render inventory item glow", error); }
    }
    private static void vertex(VertexConsumer vertices, Matrix4f pose, int x, int y, float u, float v, ItemHighlights.GuiStyle style) {
        vertices.addVertex(pose, x, y, 0).setColor(style.color()).setUv(u, v).setUv1(32, Math.round(style.radius() * 256)).setUv2(16, 16).setNormal(0, 0, 1);
    }
    private static void capture(Icon icon, LivingEntity owner, Level level, ItemStack stack, int seed) {
        org.joml.Matrix4fStack view = RenderSystem.getModelViewStack(); view.pushMatrix();
        capturing = true;
        try (var state = new LegacyItemTargets()) {
            BUFFERS.target = icon.target;
            icon.target.setClearColor(0, 0, 0, 0); icon.target.clear(); icon.target.bindWrite(true);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, 32, 32, 0, 1000, 21000), ProjectionType.ORTHOGRAPHIC);
            view.identity().translate(0, 0, -11000);
            var isolated = new GuiGraphics(Minecraft.getInstance(), BUFFERS);
            try { ((ItemGlowGuiAccessor)(Object)isolated).toolvisuals$renderItem(owner, level, stack, 8, 8, seed, 0); }
            finally { isolated.flush(); }
            icon.updated = System.nanoTime();
        } finally {
            BUFFERS.target = null; capturing = false; view.popMatrix();
        }
    }
}
