package ru.he3hauka.toolvisuals.client.minigame.ui;

import java.util.*;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient;
import ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster;
import ru.he3hauka.toolvisuals.minigame.GameKind;

import static ru.he3hauka.toolvisuals.client.minigame.ui.MiniGameLibraryScreen.text;

final class MiniGameOpponentsScreen extends VisualEditorScreen {
    private final GameKind kind;
    private final List<VisualsButton> players = new ArrayList<>();
    private List<Map.Entry<UUID, String>> rows = List.of();
    private Map<UUID, String> roster;
    private String query = "";
    private int page;
    private VisualsButton previous, next;

    MiniGameOpponentsScreen(Screen parent, GameKind kind) { super(parent, text("choose_player", text("game." + kind.key))); this.kind = kind; }
    @Override protected void init() {
        frame(580, 500); players.clear();
        field(32, 64, 516, query, 32, value -> { query = value; page = 0; roster = null; });
        for (int i = 0; i < 8; i++) {
            int row = i;
            players.add(button(26, 110 + i * 36, 528, 30, Component.empty(), UiPaint.Icon.PLAYER, () -> {
                int index = page * 8 + row;
                if (index < rows.size() && MiniGamesClient.invite(rows.get(index).getKey(), kind)) minecraft.gui.setScreen(new MiniGameWaitingScreen(parent));
            }, null));
        }
        previous = button(26, 410, 54, 30, Component.literal("<"), null, () -> page = Math.max(0, page - 1), null);
        next = button(88, 410, 54, 30, Component.literal(">"), null, () -> page++, null);
        button(410, 410, 144, 30, text("back"), UiPaint.Icon.BACK, this::onClose, null);
    }

    @Override protected void paint(GuiGraphicsExtractor g, int mx, int my) {
        var current = PlayerRoster.listedOnline(minecraft);
        if (roster != current) {
            roster = current; UUID self = minecraft.player == null ? null : minecraft.player.getUUID(); String needle = query.toLowerCase(Locale.ROOT).strip();
            rows = current.entrySet().stream().filter(entry -> !entry.getKey().equals(self) && entry.getValue().toLowerCase(Locale.ROOT).contains(needle))
                    .sorted(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER)).limit(1024).toList();
        }
        page = Math.clamp(page, 0, Math.max(0, (rows.size() - 1) / 8));
        for (int i = 0; i < players.size(); i++) {
            var button = players.get(i); int index = page * 8 + i; button.visible = index < rows.size(); button.active = MiniGamesClient.ready() && !MiniGamesClient.busy() && MiniGamesClient.room() == null;
            if (button.visible) button.setMessage(Component.literal(rows.get(index).getValue()));
        }
        previous.active = page > 0; next.active = (page + 1) * 8 < rows.size();
        UiPaint.text(g, text("page", page + 1, Math.max(1, (rows.size() + 7) / 8)), px + 160, py + 418, .8f, palette.muted, 1);
        String hint = !MiniGamesClient.supported() ? "server_required" : !MiniGamesClient.ready() ? "connecting" : rows.isEmpty() ? "no_players" : "online_hint";
        UiPaint.centered(g, text(hint).getString(), px + 26, py + 456, 528, 22, .72f, palette.muted, 1);
    }
}
