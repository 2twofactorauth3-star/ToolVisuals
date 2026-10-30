package ru.he3hauka.toolvisuals.client.replay.ui;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.replay.capture.ReplayCapture;
import ru.he3hauka.toolvisuals.client.replay.io.*;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public final class ReplayLibraryScreen extends Screen {
    private static final ExecutorService FILES = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2), task -> { var thread = new Thread(task, "ToolVisuals replay library"); thread.setDaemon(true); return thread; });
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM HH:mm").withZone(ZoneId.systemDefault());
    private final Screen parent;
    private final List<VisualsButton> buttons = new ArrayList<>();
    private List<ReplaySnapshot> recordings = List.of();
    private UiViewport viewport;
    private int px, py, pw, ph, page;
    private boolean loading;
    private String status = "";

    public ReplayLibraryScreen(Screen parent) { super(tr("title")); this.parent = parent; }
    private static Component tr(String key, Object... args) { return UiI18n.tr("toolvisuals.replay." + key, args); }

    @Override protected void init() {
        KeyMapping.releaseAll(); buttons.clear(); viewport = new UiViewport(width, height, minecraft.getWindow().getGuiScale());
        pw = Math.min(760, viewport.width - 32); ph = Math.min(500, viewport.height - 32); px = (viewport.width - pw) / 2; py = (viewport.height - ph) / 2;
        button(px + pw - 42, py + 12, 26, 26, UiI18n.tr("toolvisuals.ui.back"), ICON, UiPaint.Icon.BACK, this::onClose);
        button(px + 20, py + 58, (pw - 48) / 2, 36, tr("watch_current"), CHIP, UiPaint.Icon.CAMERA, this::current);
        button(px + 28 + (pw - 48) / 2, py + 58, (pw - 48) / 2, 36, tr("folder"), GHOST, UiPaint.Icon.WORLD,
                () -> com.mojang.blaze3d.Blaze3D.openPath(ReplayCapture.directory()));
        int rows = Math.max(1, (ph - 180) / 43); int pages = Math.max(1, (recordings.size() + rows - 1) / rows); page = Math.clamp(page, 0, pages - 1);
        for (int row = 0; row < rows && page * rows + row < recordings.size(); row++) {
            var recording = recordings.get(page * rows + row);
            String label = DATE.format(Instant.ofEpochMilli(recording.header.createdMillis)) + "  ·  " + recording.header.playerName
                    + "  ·  " + time(recording.durationMillis) + "  ·  " + String.format(java.util.Locale.ROOT, "%.1f MiB", recording.length / 1048576d);
            button(px + 20, py + 110 + row * 43, pw - 40, 36, Component.literal(label), MENU, UiPaint.Icon.CAMERA, () -> open(recording, 0));
        }
        button(px + 20, py + ph - 54, 36, 30, Component.literal("‹"), GHOST, null, () -> { page--; rebuildWidgets(); });
        button(px + 62, py + ph - 54, 36, 30, Component.literal("›"), GHOST, null, () -> { page = Math.min(page + 1, pages - 1); rebuildWidgets(); });
        button(px + pw - 148, py + ph - 54, 128, 30, tr("refresh"), CHIP, UiPaint.Icon.RESET, this::refresh);
        if (recordings.isEmpty() && !loading && status.isEmpty()) refresh();
    }

    public static String time(long millis) { long seconds = millis / 1000; return String.format(java.util.Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60); }

    private void button(int x, int y, int w, int h, Component text, VisualsButton.Kind kind, UiPaint.Icon icon, Runnable action) {
        buttons.add(addWidget(new VisualsButton(x, y, w, h, text, kind, icon, text, action, null)));
    }

    private void refresh() {
        if (loading || ReplayCapture.directory() == null) return;
        loading = true; status = tr("loading").getString();
        try {
            CompletableFuture.supplyAsync(() -> {
                try { return ReplayLibrary.list(ReplayCapture.directory()); }
                catch (java.io.IOException error) { throw new CompletionException(error); }
            }, FILES).whenComplete((files, error) -> minecraft.execute(() -> {
                loading = false;
                if (error != null) { ClientErrors.report("Refresh replay library",error);status = tr("read_error").getString(); return; }
                recordings = files; status = files.isEmpty() ? tr("empty").getString() : tr("record_hint").getString();
                if (minecraft.gui.screen() == this) rebuildWidgets();
            }));
        } catch (RejectedExecutionException error) { ClientErrors.report("Schedule replay library refresh",error);loading = false; status = tr("busy").getString(); }
    }

    private void current() {
        status = tr("loading").getString();
        ReplayCapture.snapshot().whenComplete((snapshot, error) -> minecraft.execute(() -> {
            if (minecraft.gui.screen() != this) return;
            if (error != null) status = tr("no_recording").getString();
            else open(snapshot, Math.max(0, snapshot.durationMillis - 30000));
        }));
    }

    private void open(ReplaySnapshot snapshot, long start) {
        try { minecraft.gui.setScreen(new ReplayViewerScreen(this, snapshot, start)); }
        catch (RuntimeException error) { ClientErrors.report("Open replay viewer",error);ChatMessages.notice("replay.read_error"); }
    }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) { g.fill(0, 0, width, height, 0x90070910); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
        if (width != g.guiWidth() || height != g.guiHeight()) { resize(g.guiWidth(), g.guiHeight()); return; }
        var palette = new UiPalette(ToolVisualsClient.CONFIG.lightTheme() ? 1 : 0); mx = Math.round(mx / viewport.scale); my = Math.round(my / viewport.scale);
        g.pose().pushMatrix(); g.pose().scale(viewport.scale, viewport.scale);
        UiPaint.rounded(g, px, py, pw, ph, 12, palette.panel);
        UiPaint.text(g, getTitle(), px + 20, py + 14, 1.1f, palette.text, 1);
        for (var button : buttons) { button.frame(palette, 1, 1 / 60f); button.extractRenderState(g, mx, my, partial); }
        UiPaint.centered(g, UiPaint.fit(g, Component.literal(status), pw - 40, .75f), px + 20, py + ph - 22, pw - 40, 16, .75f, palette.muted, 1);
        g.pose().popMatrix();
    }
    private MouseButtonEvent adjusted(MouseButtonEvent event) { return new MouseButtonEvent(event.x() / viewport.scale, event.y() / viewport.scale, event.buttonInfo()); }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean twice) { return super.mouseClicked(adjusted(event), twice); }
    @Override public boolean mouseReleased(MouseButtonEvent event) { return super.mouseReleased(adjusted(event)); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / viewport.scale, y / viewport.scale); }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
