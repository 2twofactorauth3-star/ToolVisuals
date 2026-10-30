package ru.he3hauka.toolvisuals.client.gui.render;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

public final class UiShapeBatch {
    private static final ThreadLocal<Capture> CURRENT = ThreadLocal.withInitial(Capture::new);
    private static final class Capture {
        final List<GuiElementRenderState> slices = new ArrayList<>(7);
        GuiRenderState owner;
        boolean active;
    }
    private UiShapeBatch() {}
    static void begin() { CURRENT.get().active = true; }
    public static boolean capture(GuiRenderState owner, GuiElementRenderState element) {
        var current = CURRENT.get();
        if (!current.active) return false;
        current.owner = owner;
        if (element.bounds() != null) current.slices.add(element);
        return true;
    }
    static void end() {
        var current = CURRENT.get(); current.active = false;
        try { if (!current.slices.isEmpty()) current.owner.addGuiElement(new Shape(current.slices)); }
        finally { current.slices.clear(); current.owner = null; }
    }
    private static final class Shape implements GuiElementRenderState {
        private final List<GuiElementRenderState> slices;
        private final ScreenRectangle bounds;
        Shape(List<GuiElementRenderState> slices) {
            this.slices = List.copyOf(slices);
            int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, right = Integer.MIN_VALUE, bottom = Integer.MIN_VALUE;
            for (var slice : slices) {
                var box = slice.bounds();
                left = Math.min(left, box.left()); top = Math.min(top, box.top());
                right = Math.max(right, box.right()); bottom = Math.max(bottom, box.bottom());
            }
            bounds = new ScreenRectangle(left, top, right - left, bottom - top);
        }
        @Override public void buildVertices(VertexConsumer consumer) { for (var slice : slices) slice.buildVertices(consumer); }
        @Override public RenderPipeline pipeline() { return slices.getFirst().pipeline(); }
        @Override public TextureSetup textureSetup() { return slices.getFirst().textureSetup(); }
        @Override public ScreenRectangle scissorArea() { return slices.getFirst().scissorArea(); }
        @Override public ScreenRectangle bounds() { return bounds; }
    }
}
