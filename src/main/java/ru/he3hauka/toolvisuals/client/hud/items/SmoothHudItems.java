package ru.he3hauka.toolvisuals.client.hud.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;

public final class SmoothHudItems {
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> false);
    private SmoothHudItems() {}
    public interface State { boolean toolvisuals$smoothItem(); }
    public static boolean extracting() { return ACTIVE.get(); }
    public static void item(GuiGraphicsExtractor g, ItemStack stack, int x, int y) {
        boolean previous = ACTIVE.get(); ACTIVE.set(true);
        try { g.item(stack, x, y); } finally { ACTIVE.set(previous); }
    }
    public static void decorations(GuiGraphicsExtractor g, ItemStack stack, int x, int y, UiPalette palette) {
        g.itemDecorations(Minecraft.getInstance().font, stack, x, y, "");
        if (stack.getCount() > 1) {
            String count = Integer.toString(stack.getCount());
            UiPaint.baseline(g, count, x + 16 - UiPaint.textWidth(g, count, .6f), y + 16, .6f, palette.text, 1);
        }
    }
}
