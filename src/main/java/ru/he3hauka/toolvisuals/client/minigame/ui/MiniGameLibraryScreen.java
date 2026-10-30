package ru.he3hauka.toolvisuals.client.minigame.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient;
import ru.he3hauka.toolvisuals.minigame.GameKind;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.MINI_GAMES;

public final class MiniGameLibraryScreen extends VisualEditorScreen {
    private GameKind selected = GameKind.ANGRY_BIRDS;
    private VisualsButton resume, solo, multiplayer;
    public MiniGameLibraryScreen(Screen parent) { super(parent, text("title")); }
    static Component text(String key, Object... arguments) { return UiI18n.tr("toolvisuals.minigames." + key, arguments); }

    @Override protected void init() {
        frame(760, 460);
        button(556, 60, 180, 28, text("enabled"), UiPaint.Icon.CHECK,
                () -> ToolVisualsClient.CONFIG.setActive(MINI_GAMES, !ToolVisualsClient.CONFIG.active(MINI_GAMES)), () -> ToolVisualsClient.CONFIG.active(MINI_GAMES));
        for (var kind : GameKind.values()) button(26 + kind.id * 240, 288, 216, 34, text("select"), null, () -> selected = kind, () -> selected == kind);
        solo = button(26, 348, 216, 36, text("solo"), UiPaint.Icon.PLAYER, () -> MiniGamesClient.startSolo(selected), null);
        multiplayer = button(266, 348, 216, 36, text("multiplayer"), UiPaint.Icon.WORLD, () -> {
            ToolVisualsClient.CONFIG.setActive(MINI_GAMES, true); MiniGamesClient.stop(); minecraft.gui.setScreen(new MiniGameOpponentsScreen(this, selected));
        }, null);
        resume = button(506, 348, 106, 36, text("resume"), UiPaint.Icon.ARROW, () -> {
            if (MiniGamesClient.round() != null) minecraft.gui.setScreen(new MiniGamePlayScreen(this));
        }, null);
        button(624, 348, 108, 36, text("back"), UiPaint.Icon.BACK, this::onClose, null);
    }

    @Override protected void paint(GuiGraphicsExtractor g, int mx, int my) {
        UiPaint.text(g, text("subtitle"), px + 26, py + 66, .8f, palette.muted, 1);
        for (var kind : GameKind.values()) {
            int x = px + 26 + kind.id * 240;
            UiPaint.rounded(g, x, py + 104, 216, 170, 10, selected == kind ? palette.selected : palette.raised);
            UiPaint.text(g, text("game." + kind.key), x + 14, py + 118, .95f, selected == kind ? palette.accentText : palette.text, 1);
            MiniGamePaint.preview(g, kind, x + 10, py + 158, palette);
            UiPaint.centered(g, text("short." + kind.key).getString(), x + 8, py + 246, 200, 20, .72f, palette.muted, 1);
        }
        String status = minecraft.player == null ? "join_world" : !ToolVisualsClient.CONFIG.active(MINI_GAMES) ? "disabled_hint" : !MiniGamesClient.supported() ? "server_required" : MiniGamesClient.ready() ? "online_hint" : "connecting";
        UiPaint.centered(g, text(status).getString(), px + 26, py + 405, 708, 24, .77f, palette.muted, 1);
        resume.active = MiniGamesClient.round() != null;
        solo.active = multiplayer.active = minecraft.player != null;
    }
}
