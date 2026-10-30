package ru.he3hauka.toolvisuals.client.replay.ui;

import java.util.HashSet;
import java.util.Set;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.replay.io.ReplaySnapshot;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayPlayback;

public final class ReplayViewerScreen extends Screen {
    private static final double[] SPEEDS = {.25, .5, 1, 2, 4};
    private final Screen parent;
    private final ReplaySnapshot snapshot;
    private final long start;
    private final Set<Integer> keys = new HashSet<>();
    private ReplayPlayback playback;
    private long requested = -1, requestedAt;
    private boolean rotating, timeline, failed;

    public ReplayViewerScreen(Screen parent, ReplaySnapshot snapshot, long start) {
        super(Component.literal("ToolVisuals Replay")); this.parent = parent; this.snapshot = snapshot; this.start = start;
    }

    @Override protected void init() {
        KeyMapping.releaseAll();
        if (playback == null && !failed) try { playback = new ReplayPlayback(snapshot, this, start); }
        catch (Exception error) { failure(error); }
    }

    public void frame() throws java.io.IOException {
        if (playback == null) return;
        if (requested >= 0 && !timeline && System.nanoTime() - requestedAt > 120000000) {
            playback.seek(requested); requested = -1;
        }
        playback.update();
    }

    public void renderWorld(Runnable action) { if (playback != null) playback.render(action); else action.run(); }
    public boolean ready() { return playback != null && playback.ready(); }
    public double position() { return playback == null ? 0 : playback.position(); }

    public void failure(Throwable error) {
        if (failed) return;
        failed = true;
        org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Replay viewer stopped; live world restored", error);
        if (playback != null) { playback.close(); playback = null; }
        if (minecraft != null) { minecraft.gui.setScreen(parent); ChatMessages.notice("replay.read_error"); }
    }

    @Override public void tick() {
        if (playback == null || !minecraft.isWindowActive()) { keys.clear(); return; }
        double speed = keys.contains(InputConstants.KEY_LSHIFT) ? 1.5 : .35;
        playback.move((pressed(InputConstants.KEY_W) - pressed(InputConstants.KEY_S)) * speed,
                (pressed(InputConstants.KEY_A) - pressed(InputConstants.KEY_D)) * speed,
                (pressed(InputConstants.KEY_SPACE) - pressed(InputConstants.KEY_LCONTROL)) * speed, 0, 0);
    }

    private int pressed(int key) { return keys.contains(key) ? 1 : 0; }
    private String tr(String key, Object... args) { return UiI18n.tr("toolvisuals.replay." + key, args).getString(); }
    private float left() { return Math.max(8, (width - 600) / 2f); }
    private float panelWidth() { return Math.min(600, width - 16); }

    @Override public void extractBackground(GuiGraphicsExtractor g, int x, int y, float partial) { if (!ready()) g.fill(0, 0, width, height, 0xff111218); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float partial) {
        var palette = new UiPalette(ToolVisualsClient.CONFIG.lightTheme() ? 1 : 0);
        if (!ready()) UiPaint.centered(g, tr("loading"), 0, height / 2f - 15, width, 30, 1, palette.text, 1);
        float left = left(), w = panelWidth(), top = height - 75;
        UiPaint.rounded(g, left, top, w, 66, 9, palette.hudPanel);
        long position = requested >= 0 ? requested : playback == null ? 0 : playback.position();
        UiPaint.text(g, ReplayLibraryScreen.time(position) + " / " + ReplayLibraryScreen.time(snapshot.durationMillis), left + 12, top + 7, .8f, palette.text, 1);
        UiPaint.text(g, tr(playback != null && playback.seeking() ? "seeking" : "controls"), left + 12, top - 22, .7f, palette.muted, 1);
        float progress = snapshot.durationMillis == 0 ? 0 : position / (float)snapshot.durationMillis;
        UiPaint.rounded(g, left + 12, top + 26, w - 24, 4, 2, palette.track);
        UiPaint.rounded(g, left + 12, top + 26, (w - 24) * Math.clamp(progress, 0, 1), 4, 2, palette.accent);
        UiPaint.rounded(g, left + 8 + (w - 24) * Math.clamp(progress, 0, 1), top + 23, 10, 10, 5, palette.accentText);
        String[] labels = {"−5 s", tr(playback != null && playback.clock.playing() ? "pause" : "play"), "+5 s",
                playback == null ? "1×" : playback.clock.speed() + "×", tr("follow"), tr("back")};
        float bw = (w - 24) / labels.length;
        for (int index = 0; index < labels.length; index++) {
            float bx = left + 12 + index * bw;
            if (x >= bx && x < bx + bw && y >= top + 37 && y < top + 62) UiPaint.rounded(g, bx, top + 37, bw - 3, 24, 5, palette.hover);
            UiPaint.centered(g, labels[index], bx, top + 37, bw - 3, 24, .75f, palette.text, 1);
        }
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean twice) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) { rotating = true; return true; }
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || playback == null) return true;
        float left = left(), w = panelWidth(), top = height - 75;
        if (event.x() >= left + 8 && event.x() <= left + w - 8 && event.y() >= top + 20 && event.y() <= top + 35) {
            timeline = true; requestAt(event.x()); return true;
        }
        if (event.x()>=left+12&&event.x()<left+w-12&&event.y() >= top + 37 && event.y() < top + 62) {
            int index = (int)((event.x() - left - 12) / ((w - 24) / 6));
            switch (index) {
                case 0 -> request(playback.position() - 5000);
                case 1 -> playback.toggle();
                case 2 -> request(playback.position() + 5000);
                case 3 -> { int next = 0; while (next < SPEEDS.length && SPEEDS[next] <= playback.clock.speed()) next++; playback.speed(SPEEDS[next % SPEEDS.length]); }
                case 4 -> playback.follow();
                case 5 -> onClose();
                default -> {}
            }
        }
        return true;
    }

    private void request(long millis) { requested = Math.clamp(millis, 0, snapshot.durationMillis); requestedAt = System.nanoTime(); }
    private void requestAt(double x) { request(Math.round(Math.clamp((x - left() - 12) / (panelWidth() - 24), 0, 1) * snapshot.durationMillis)); }
    @Override public boolean mouseDragged(MouseButtonEvent event, double x, double y) {
        if (rotating && playback != null) playback.move(0, 0, 0, (float)x * .25f, (float)y * .25f);
        else if (timeline) requestAt(event.x());
        return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) { rotating = false; timeline = false; return true; }
    @Override public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) { onClose(); return true; }
        if (event.key() == InputConstants.KEY_P && playback != null) { playback.toggle(); return true; }
        keys.add(event.key()); return true;
    }
    @Override public boolean keyReleased(KeyEvent event) { keys.remove(event.key()); return true; }
    @Override public void removed() { keys.clear(); if (playback != null) { playback.close(); playback = null; } }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
