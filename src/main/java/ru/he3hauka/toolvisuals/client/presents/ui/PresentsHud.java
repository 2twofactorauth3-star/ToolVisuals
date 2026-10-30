package ru.he3hauka.toolvisuals.client.presents.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.presents.PresentCatalog;
import ru.he3hauka.toolvisuals.client.presents.ToolPresents;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire.Area;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class PresentsHud {
    private PresentsHud() {}

    public static void draw(GuiGraphicsExtractor g, UiPalette palette) {
        if (!ToolPresents.active() || !ToolVisualsClient.CONFIG.flag(PRESENTS_HUD)) return;
        var client = Minecraft.getInstance();
        var route = ToolPresents.route();
        String title = "ToolPresents  " + ToolPresents.collected() + "/" + PresentCatalog.ALL.size();
        String targetKey;
        if (ToolPresents.collected() == PresentCatalog.ALL.size()) targetKey = "complete";
        else if (ToolPresents.area() == Area.ARENA && !ToolPresents.spawnComplete()) targetKey = "spawn_required";
        else if (ToolPresents.area() == Area.SPAWN && ToolPresents.spawnComplete()) targetKey = "arena_pending";
        else targetKey = route.isEmpty() ? "planning" : "next";
        String target = UiI18n.tr("toolvisuals.presents." + targetKey, route.isEmpty() ? 0 : route.getFirst().id()).getString();
        String details = "";
        double direction = 0;
        if (!route.isEmpty()) {
            var point = route.getFirst();
            var position = new Vec3(point.x() + .5, point.y() + .5, point.z() + .5);
            long distance = Math.round(position.distanceTo(client.player.position()));
            details = point.x() + "  " + point.y() + "  " + point.z() + "  ·  " + distance + " m";
            direction = Math.toRadians(Mth.wrapDegrees(Math.toDegrees(Math.atan2(position.z - client.player.getZ(), position.x - client.player.getX())) - 90 - client.player.getYRot()));
        }
        float scale = ToolVisualsClient.CONFIG.number(PRESENTS_SCALE) / client.getWindow().getGuiScale();
        g.pose().pushMatrix(); g.pose().scale(scale, scale);
        float w = Math.max(210, Math.max(UiPaint.textWidth(g, title, .9f),
                Math.max(UiPaint.textWidth(g, details, .8f), UiPaint.textWidth(g, target, .8f))) + 44);
        float h = details.isEmpty() ? 46 : 64;
        float x = (g.guiWidth() / scale - w) * ToolVisualsClient.CONFIG.number(PRESENTS_X) / 100;
        float y = (g.guiHeight() / scale - h) * ToolVisualsClient.CONFIG.number(PRESENTS_Y) / 100;
        HudPlacement.place(g, HudElement.PRESENTS, x, y, w, h);
        UiPaint.rounded(g, x, y, w, h, 8, palette.hudPanel);
        UiPaint.text(g, title, x + 12, y + 7, .9f, palette.accentText, 1);
        UiPaint.text(g, target, x + 12, y + 25, .8f, palette.text, 1);
        if (!details.isEmpty()) {
            UiPaint.text(g, details, x + 12, y + 43, .8f, palette.muted, 1);
            float cx = x + w - 20, cy = y + 31;
            g.pose().pushMatrix(); g.pose().translate(cx, cy); g.pose().rotate((float)(direction - Math.PI / 2));
            UiPaint.icon(g, UiPaint.Icon.ARROW, -10, -10, 20, palette.accentText, 1);
            g.pose().popMatrix();
        }
        g.pose().popMatrix(); g.pose().popMatrix();
    }
}
