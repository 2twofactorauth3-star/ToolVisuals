package ru.he3hauka.toolvisuals.client.social.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

abstract class SocialScreen extends Screen {
    private final int panelHeight;
    private final Motion entrance = new Motion(0);
    private final List<VisualsButton> buttons = new ArrayList<>();
    protected float scale, left, top;
    private long frame;
    SocialScreen(String title, int panelHeight) { super(tr(title)); this.panelHeight = panelHeight; }
    protected static Component tr(String key, Object... args) { return UiI18n.tr("toolvisuals.pairs." + key, args); }
    @Override protected final void init() {
        var viewport = new UiViewport(width, height, minecraft.getWindow().getGuiScale());
        scale = Math.min(viewport.scale, Math.min(width / 512f, height / (panelHeight + 32f)));
        left = width / scale / 2 - 240; top = height / scale / 2 - panelHeight / 2f; frame = System.nanoTime(); buttons.clear(); build();
    }
    protected abstract void build();
    protected abstract void body(GuiGraphicsExtractor graphics, UiPalette palette, float opacity);
    protected VisualsButton button(float x, float y, int width, Component text, Runnable action) {
        var button = addWidget(new VisualsButton(Math.round(left + x), Math.round(top + y), width, 34, text, VisualsButton.Kind.CHIP, null, Component.empty(), action, null));
        buttons.add(button); return button;
    }
    protected void line(GuiGraphicsExtractor graphics, String text, float y, float size, int color, float opacity) {
        UiPaint.centered(graphics, UiPaint.fit(graphics, Component.literal(text), 432, size), left + 24, top + y, 432, 24, size, color, opacity);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) { g.fill(0, 0, width, height, 0x99080812); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
        long now = System.nanoTime(); float elapsed = Math.clamp((now - frame) / 1e9f, 0, .1f); frame = now;
        float opacity = entrance.step(1, elapsed, 16); var palette = new UiPalette(ToolVisualsClient.CONFIG.lightTheme() ? 1 : 0);
        g.pose().pushMatrix(); g.pose().scale(scale, scale);
        UiPaint.rounded(g, left, top, 480, panelHeight, 14, UiPaint.alpha(palette.panel, opacity));
        line(g, getTitle().getString(), 18, 1.25f, palette.text, opacity); body(g, palette, opacity);
        for (var button : buttons) { button.frame(palette, opacity, elapsed); button.extractRenderState(g, Math.round(mx / scale), Math.round(my / scale), partial); }
        g.pose().popMatrix();
    }
    private MouseButtonEvent adjusted(MouseButtonEvent event) { return new MouseButtonEvent(event.x() / scale, event.y() / scale, event.buttonInfo()); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean twice) { return super.mouseClicked(adjusted(event), twice); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(adjusted(event)); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / scale, y / scale); }
}
