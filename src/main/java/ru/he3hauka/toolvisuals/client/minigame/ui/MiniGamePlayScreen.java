package ru.he3hauka.toolvisuals.client.minigame.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient;
import ru.he3hauka.toolvisuals.client.social.SocialClient;
import ru.he3hauka.toolvisuals.minigame.GameKind;
import ru.he3hauka.toolvisuals.minigame.GameRound;
import ru.he3hauka.toolvisuals.minigame.sling.SlingGame;

import static ru.he3hauka.toolvisuals.client.minigame.ui.MiniGameLibraryScreen.text;
import static ru.he3hauka.toolvisuals.client.minigame.ui.MiniGamePaint.*;

public final class MiniGamePlayScreen extends VisualEditorScreen {
    private boolean dragging;
    private int angle = 43, power = 95;
    private VisualsButton newGame;
    public MiniGamePlayScreen(Screen parent) { super(parent, text(MiniGamesClient.round() == null ? "title" : "game." + MiniGamesClient.round().kind.key)); }

    @Override protected void init() {
        frame(760, 560);
        button(26, 508, 218, 32, text("library"), UiPaint.Icon.BACK, () -> minecraft.gui.setScreen(new MiniGameLibraryScreen(parent)), null);
        newGame = button(270, 508, 218, 32, text("new_game"), UiPaint.Icon.RESET, () -> { var round = MiniGamesClient.round(); if (round != null && MiniGamesClient.solo()) MiniGamesClient.startSolo(round.kind); }, null);
        button(514, 508, 218, 32, text("finish"), UiPaint.Icon.CLOSE, () -> { MiniGamesClient.stop(); minecraft.gui.setScreen(new MiniGameLibraryScreen(parent)); }, null);
    }

    @Override protected void paint(GuiGraphicsExtractor g, int mx, int my) {
        var round = MiniGamesClient.round(); if (round == null) return;
        newGame.active = MiniGamesClient.solo();
        String opponent = MiniGamesClient.solo() ? text("computer").getString() : opponent();
        UiPaint.text(g, text("versus", opponent), px + 26, py + 65, .9f, palette.muted, 1);
        UiPaint.text(g, text("move_number", round.revision), px + 588, py + 65, .78f, palette.faint, 1);
        String status = MiniGamesClient.animating() ? "flight" : round.winner == 3 ? "draw" : round.winner != 0 ? round.winner == MiniGamesClient.ownTurn() ? "won" : "lost"
                : MiniGamesClient.canPlay() ? "your_turn" : MiniGamesClient.solo() ? "computer_turn" : "other_turn";
        UiPaint.centered(g, text(status).getString(), px + 26, py + 96, 708, 24, 1.05f, round.winner == 0 ? palette.accentText : 0xffffc274, 1);
        if (round.kind == GameKind.ANGRY_BIRDS) sling(g, round);
        else grid(g, round, mx, my);
        UiPaint.centered(g, text(round.kind == GameKind.ANGRY_BIRDS ? "sling_hint" : "grid_hint").getString(), px + 26, py + 472, 708, 24, .76f, palette.muted, 1);
    }

    private String opponent() {
        var room = MiniGamesClient.room(); if (room == null) return "";
        return SocialClient.name(MiniGamesClient.ownTurn() == 1 ? room.second() : room.first());
    }

    private void grid(GuiGraphicsExtractor g, GameRound round, int mx, int my) {
        boolean tic = round.kind == GameKind.TIC_TAC_TOE; int cell = tic ? 88 : 48;
        int left = px + (760 - round.grid.width * cell) / 2, top = py + (tic ? 150 : 146);
        UiPaint.rounded(g, left - 12, top - 12, round.grid.width * cell + 24, round.grid.height * cell + 24, 14, palette.inset);
        for (int y = 0; y < round.grid.height; y++) for (int x = 0; x < round.grid.width; x++) {
            float bx = left + x * cell + 4, by = top + y * cell + 4; int value = round.grid.cell(x, y);
            boolean hover = MiniGamesClient.canPlay() && mx >= left - px + x * cell && mx < left - px + (x + 1) * cell
                    && my >= top - py + (tic ? y * cell : 0) && my < top - py + (tic ? (y + 1) * cell : round.grid.height * cell);
            UiPaint.rounded(g, bx, by, cell - 8, cell - 8, tic ? 9 : (cell - 8) / 2f, hover && value == 0 ? palette.hover : palette.button);
            if (value == 0) continue;
            int color = value == 1 ? palette.accentText : 0xffffc274;
            float cx = bx + (cell - 8) / 2f, cy = by + (cell - 8) / 2f;
            if (!tic) disk(g, cx, cy, 16, color);
            else if (value == 1) { line(g, cx - 18, cy - 18, cx + 18, cy + 18, 5, color); line(g, cx + 18, cy - 18, cx - 18, cy + 18, 5, color); }
            else { disk(g, cx, cy, 24, color); disk(g, cx, cy, 18, palette.button); }
        }
    }

    private void sling(GuiGraphicsExtractor g, GameRound round) {
        float x = px + 50, y = py + 148;
        double time = MiniGamesClient.animationTime(); boolean flight = MiniGamesClient.animating();
        var shot = round.shot;
        var level = flight && shot != null ? shot.before() : round.winner != 0 ? MiniGamesClient.ownTurn() == 1 ? round.first : round.second : round.turn == 1 ? round.first : round.second;
        UiPaint.rounded(g, x - 4, y - 4, SlingGame.WIDTH + 8, SlingGame.HEIGHT + 8, 12, palette.inset);
        g.enableScissor((int)x, (int)y, (int)x + SlingGame.WIDTH, (int)y + SlingGame.HEIGHT);
        UiPaint.rounded(g, x, y, SlingGame.WIDTH, SlingGame.HEIGHT, 9, 0xff1b2941);
        UiPaint.rounded(g, x - 25, y + 174, 370, 170, 85, 0xff213d4b);
        UiPaint.rounded(g, x + 260, y + 192, 450, 170, 85, 0xff2b4856);
        UiPaint.rounded(g, x, y + SlingGame.GROUND, SlingGame.WIDTH, 30, 0, 0xff537465);
        UiPaint.rounded(g, x, y + SlingGame.GROUND, SlingGame.WIDTH, 3, 0, 0xffa7c594);
        for (int i = 0; i < level.wood().size(); i++) {
            var block = level.wood().get(i); int index = i;
            boolean broken = flight && shot.impacts().stream().anyMatch(hit -> hit.wood() == index && hit.time() <= time);
            if (block.health() > 0 && !broken) wood(g, block, x, y);
        }
        for (int i = 0; i < level.pigs().size(); i++) {
            var pig = level.pigs().get(i); int index = i;
            boolean gone = flight && shot.impacts().stream().anyMatch(hit -> hit.pig() == index && hit.time() <= time);
            if (pig.alive() && !gone) pig(g, x + pig.x(), y + pig.y(), 15);
        }
        line(g, x + 65, y + 265, x + 62, y + 216, 10, 0xffb88a64);
        line(g, x + 65, y + 236, x + 81, y + 213, 8, 0xffd2a178);
        if (flight && shot != null) {
            var path = shot.path(); SlingGame.Point current = path.get(0);
            for (int i = 1; i < path.size(); i++) {
                var point = path.get(i); if (point.time() > time) {
                    var before = path.get(i - 1); double t = Math.clamp((time - before.time()) / Math.max(.001, point.time() - before.time()), 0, 1);
                    current = new SlingGame.Point(before.x() + (point.x() - before.x()) * t, before.y() + (point.y() - before.y()) * t, time); break;
                }
                current = point; if (i % 10 == 0) disk(g, x + point.x(), y + point.y(), 1.8, 0x88d6c9ff);
            }
            bird(g, x + current.x(), y + current.y(), 9);
            for (var impact : shot.impacts()) {
                double age = time - impact.time(); if (age < 0 || age > .8) continue;
                for (int i = 0; i < 6; i++) {
                    double theta = i * Math.PI / 3, radius = 8 + age * 30;
                    disk(g, x + impact.x() + Math.cos(theta) * radius, y + impact.y() + Math.sin(theta) * radius, 3 * (1 - age / .8), UiPaint.alpha(impact.pig() >= 0 ? 0xffa9efac : 0xffe8ba80, (float)(1 - age / .8)));
                }
                UiPaint.text(g, impact.pig() >= 0 ? "+500" : "+40", x + (float)impact.x() - 12, y + (float)(impact.y() - 20 - age * 24), .7f, 0xffdcebb5, (float)(1 - age / .8));
            }
        } else {
            double radians = Math.toRadians(angle), length = dragging ? power / 1.4 : 0;
            double bx = x + SlingGame.X - Math.cos(radians) * length, by = y + SlingGame.Y + Math.sin(radians) * length;
            line(g, x + 62, y + 216, bx, by, 4, 0xff785368); bird(g, bx, by, 10); line(g, bx, by, x + 81, y + 213, 3, 0xffa56d84);
            if (dragging) for (int i = 1; i <= 22; i++) {
                double t = i * .07, tx = SlingGame.X + Math.cos(radians) * power * 4.6 * t, ty = SlingGame.Y - Math.sin(radians) * power * 4.6 * t + 175 * t * t;
                if (ty < SlingGame.GROUND) disk(g, x + tx, y + ty, 2, 0x99e1d5ff);
            }
        }
        int firstScore = round.first.score(), secondScore = round.second.score();
        if (flight && shot != null) {
            int currentScore = shot.before().score();
            for (var impact : shot.impacts()) if (impact.time() <= time) currentScore += impact.pig() >= 0 ? 500 : 40;
            if (round.revision % 2 == 1) firstScore = currentScore; else secondScore = currentScore;
        }
        UiPaint.text(g, text("score", MiniGamesClient.ownTurn() == 1 ? firstScore : secondScore, MiniGamesClient.ownTurn() == 1 ? secondScore : firstScore), x + 14, y + 12, .75f, 0xffe7e5fa, 1);
        UiPaint.text(g, text("birds", Math.max(0, 3 - (round.revision + (round.turn == 1 ? 1 : 0)) / 2)), x + 480, y + 12, .75f, 0xffe7e5fa, 1);
        g.disableScissor();
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean twice) {
        var round = MiniGamesClient.round();
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && round != null && MiniGamesClient.canPlay()) {
            double x = event.x() / scale - px, y = event.y() / scale - py;
            if (round.kind == GameKind.ANGRY_BIRDS) {
                if (Math.hypot(x - 50 - SlingGame.X, y - 148 - SlingGame.Y) < 38) { dragging = true; aim(x, y); return true; }
            } else {
                int cell = round.kind == GameKind.TIC_TAC_TOE ? 88 : 48, left = (760 - round.grid.width * cell) / 2, top = round.kind == GameKind.TIC_TAC_TOE ? 150 : 146;
                if (x >= left && x < left + round.grid.width * cell && y >= top && y < top + round.grid.height * cell) {
                    int column = (int)(x - left) / cell, row = (int)(y - top) / cell, move = round.kind == GameKind.TIC_TAC_TOE ? row * 3 + column : column;
                    if (round.grid.legal(move)) MiniGamesClient.move(move, 0); return true;
                }
            }
        }
        return super.mouseClicked(event, twice);
    }

    private void aim(double x, double y) {
        double dx = Math.max(1, 50 + SlingGame.X - x), dy = Math.max(1, y - 148 - SlingGame.Y);
        angle = (int)Math.clamp(Math.round(Math.toDegrees(Math.atan2(dy, dx))), 5, 85);
        power = (int)Math.clamp(Math.round(Math.hypot(dx, dy) * 1.4), 10, 100);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging) { aim(event.x() / scale - px, event.y() / scale - py); return true; }
        return super.mouseDragged(event, dx, dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            dragging = false; aim(event.x() / scale - px, event.y() / scale - py);
            if (MiniGamesClient.canPlay()) MiniGamesClient.move(angle, power); return true;
        }
        return super.mouseReleased(event);
    }
    @Override public void tick() { if (MiniGamesClient.round() == null) minecraft.gui.setScreen(new MiniGameLibraryScreen(parent)); }
}
