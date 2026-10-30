package ru.he3hauka.toolvisuals.client.minigame.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient;

import static ru.he3hauka.toolvisuals.client.minigame.ui.MiniGameLibraryScreen.text;

final class MiniGameWaitingScreen extends VisualEditorScreen {
    MiniGameWaitingScreen(Screen parent) { super(parent, text("waiting_title")); }
    @Override protected void init() {
        frame(520, 260);
        button(28, 196, 218, 36, text("cancel"), UiPaint.Icon.CLOSE, () -> { MiniGamesClient.stop(); minecraft.gui.setScreen(parent); }, null);
        button(266, 196, 226, 36, text("return_game"), UiPaint.Icon.BACK, () -> minecraft.gui.setScreen(null), null);
    }
    @Override protected void paint(GuiGraphicsExtractor g, int mx, int my) {
        UiPaint.centered(g, text("waiting").getString(), px + 24, py + 88, 472, 30, 1, palette.accentText, 1);
        UiPaint.centered(g, text("waiting_hint").getString(), px + 24, py + 131, 472, 24, .78f, palette.muted, 1);
    }
    @Override public void tick() { if (!MiniGamesClient.busy() && MiniGamesClient.room() == null) minecraft.gui.setScreen(parent); }
    @Override public void onClose() { MiniGamesClient.stop(); super.onClose(); }
}
