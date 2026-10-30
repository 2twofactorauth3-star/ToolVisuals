package ru.he3hauka.toolvisuals.client.hud.info;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.utility.privacy.PrivacyMode;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class InfoHudPanel {
    private record Metric(InfoHudPart part, String text) {}
    private static final float PADDING = 8, MARGIN = 12, ROW_HEIGHT = 26, COORDINATES_HEIGHT = 22;
    private static long updated = Long.MIN_VALUE, revision = -1;
    private static Object connection;
    private static List<Metric> joined = List.of(), separated = List.of();
    private static Metric coordinates;

    private InfoHudPanel() {}

    private static void update(Minecraft client, ClientConfig config) {
        long now = System.nanoTime();
        if (connection == client.getConnection() && revision == config.revision()
                && updated != Long.MIN_VALUE && now - updated < 100_000_000L) return;
        connection = client.getConnection(); revision = config.revision(); updated = now;
        var nextJoined = new ArrayList<Metric>();
        var nextSeparated = new ArrayList<Metric>();
        coordinates = null;
        for (var part : InfoHudPart.ALL) {
            if (!part.enabled(config)) continue;
            String text = switch (part) {
                case BRAND -> "ToolVisuals";
                case FPS -> Math.max(0, client.getFps()) + " FPS";
                case BPS -> InfoHudValues.speed(client.player.getX() - client.player.xOld, client.player.getZ() - client.player.zOld);
                case PING -> {
                    var info = client.getConnection() == null ? null : client.getConnection().getPlayerInfo(client.player.getUUID());
                    yield InfoHudValues.ping(info == null ? -1 : info.getLatency());
                }
                case CLOCK -> InfoHudValues.clock(Instant.now(), ZoneId.systemDefault(), config.flag(HUD_CLOCK_SECONDS));
                case COORDINATES -> String.format(Locale.ROOT, "XYZ  %.1f  /  %.1f  /  %.1f", client.player.getX(), client.player.getY(), client.player.getZ());
            };
            var metric = new Metric(part, text);
            if (config.flag(part.separate)) nextSeparated.add(metric);
            else if (part == InfoHudPart.COORDINATES) coordinates = metric;
            else nextJoined.add(metric);
        }
        joined = List.copyOf(nextJoined);
        nextSeparated.sort(Comparator.comparingInt(metric -> metric.part.defaultOrder));
        separated = List.copyOf(nextSeparated);
    }

    public static void draw(GuiGraphicsExtractor graphics, Minecraft client, ClientConfig config, UiPalette palette) {
        update(client, config);
        boolean showCoordinates = !PrivacyMode.coordinates();
        drawJoined(graphics, palette, showCoordinates);
        float bottom = graphics.guiHeight() / graphics.pose().m11() - MARGIN;
        for (var metric : separated) {
            if (metric.part == InfoHudPart.COORDINATES && !showCoordinates) continue;
            float width = contentWidth(graphics, metric) + PADDING * 2, height = height(metric);
            float x = MARGIN, y = bottom - height;
            bottom = y - 6;
            HudPlacement.place(graphics, metric.part.element, x, y, width, height);
            try { drawCapsule(graphics, metric, x, y, width, palette); }
            finally { graphics.pose().popMatrix(); }
        }
    }

    private static void drawJoined(GuiGraphicsExtractor graphics, UiPalette palette, boolean showCoordinates) {
        boolean hasCoordinates = coordinates != null && showCoordinates;
        if (joined.isEmpty() && !hasCoordinates) return;
        float rowWidth = joined.isEmpty() ? 0 : PADDING * 2 + (joined.size() - 1) * 14;
        for (var metric : joined) rowWidth += contentWidth(graphics, metric);
        float coordinatesWidth = hasCoordinates ? contentWidth(graphics, coordinates) + PADDING * 2 : 0;
        float width = Math.max(rowWidth, coordinatesWidth);
        float height = joined.isEmpty() ? COORDINATES_HEIGHT : ROW_HEIGHT + (hasCoordinates ? 4 + COORDINATES_HEIGHT : 0);
        float x = MARGIN, y = MARGIN;
        HudPlacement.place(graphics, HudElement.INFO, x, y, width, height);
        try {
            if (!joined.isEmpty()) {
                float left = x, cursor = left + PADDING;
                UiPaint.rounded(graphics, left, y, rowWidth, ROW_HEIGHT, 7, palette.hudPanel);
                for (int index = 0; index < joined.size(); index++) {
                    var metric = joined.get(index);
                    if (index > 0) UiPaint.rounded(graphics, cursor - 7, y + 8, 1, 10, .5f, UiPaint.alpha(palette.muted, .2f));
                    drawContent(graphics, metric, cursor, y, palette);
                    cursor += contentWidth(graphics, metric) + 14;
                }
            }
            if (hasCoordinates) drawCapsule(graphics, coordinates, x,
                    y + (joined.isEmpty() ? 0 : ROW_HEIGHT + 4), coordinatesWidth, palette);
        } finally { graphics.pose().popMatrix(); }
    }

    private static float textScale(Metric metric) { return metric.part == InfoHudPart.COORDINATES ? .9f : .95f; }
    private static float height(Metric metric) { return metric.part == InfoHudPart.COORDINATES ? COORDINATES_HEIGHT : ROW_HEIGHT; }
    private static float contentWidth(GuiGraphicsExtractor graphics, Metric metric) {
        return UiPaint.textWidth(graphics, metric.text, textScale(metric)) + (metric.part.icon == null ? 0 : 16);
    }

    private static void drawCapsule(GuiGraphicsExtractor graphics, Metric metric, float x, float y, float width, UiPalette palette) {
        UiPaint.rounded(graphics, x, y, width, height(metric), metric.part == InfoHudPart.COORDINATES ? 6 : 7, palette.hudPanel);
        drawContent(graphics, metric, x + PADDING, y, palette);
    }

    private static void drawContent(GuiGraphicsExtractor graphics, Metric metric, float x, float y, UiPalette palette) {
        if (metric.part.icon != null) {
            UiPaint.icon(graphics, metric.part.icon, x, y + 7, 12, palette.accentText, 1);
            x += 16;
        }
        float scale = textScale(metric);
        UiPaint.baseline(graphics, metric.text, x, UiPaint.textBaseline(graphics, y, height(metric), scale), scale,
                metric.part == InfoHudPart.COORDINATES ? palette.muted : palette.text, 1);
    }
}
