package ru.he3hauka.toolvisuals.client.hud;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.crosshair.CrosshairPainter;
import ru.he3hauka.toolvisuals.client.friends.ui.FriendsListHud;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.render.UiSprites;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.armor.ArmorHud;
import ru.he3hauka.toolvisuals.client.hud.cooldown.CooldownHud;
import ru.he3hauka.toolvisuals.client.hud.inventory.InventoryHud;
import ru.he3hauka.toolvisuals.client.hud.layout.HudEditorScreen;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.hud.modules.ModuleListLimit;
import ru.he3hauka.toolvisuals.client.hud.modules.ModuleListOrder;
import ru.he3hauka.toolvisuals.client.hud.tab.ExtraTab;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.waypoint.WorldLabels;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class VisualHud {
    private static final float MODULE_TEXT_SCALE = 1.25f;
    private static final int MODULE_HEIGHT = 26, MODULE_GAP = 1;
    private static final UiPalette DARK = new UiPalette(0), LIGHT = new UiPalette(1);
    private record ModuleChip(String label, float width) {}
    private static List<ModuleChip> moduleChips = List.of();
    private static long moduleRevision = -1;
    private static float moduleDensity;
    private static String moduleUser = "";
    private VisualHud() {}
    public static void draw(GuiGraphicsExtractor graphics) {
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;
        var client = Minecraft.getInstance();
        if (client.player == null || !HudPlacement.preview() && (client.gui.screen() instanceof HudEditorScreen
                || ExtraTab.showing() || client.gui.hud.isHidden() || NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.ALL_HUD))) return;
        var config = ToolVisualsClient.CONFIG;
        var palette = config.lightTheme() ? LIGHT : DARK;
        WorldLabels.draw(graphics,palette);
        ru.he3hauka.toolvisuals.client.presents.ui.PresentsHud.draw(graphics,palette);
        ExtraHud.draw(graphics, palette);
        if(HudPlacement.show(HEALTH_INDICATOR))ru.he3hauka.toolvisuals.client.hud.health.HealthIndicator.draw(graphics,palette);
        if (HudPlacement.show(COOLDOWN_HUD)) CooldownHud.draw(graphics,palette);
        if (HudPlacement.show(FRIENDS_LIST)) FriendsListHud.draw(graphics,palette);
        if (HudPlacement.show(CLIENT_LIST)) ru.he3hauka.toolvisuals.client.social.presence.ui.ClientListHud.draw(graphics,palette);
        if (HudPlacement.show(SESSION_STATS)) ru.he3hauka.toolvisuals.client.session.ui.SessionStatsHud.draw(graphics,palette);
        if (HudPlacement.show(MUSIC_HUD)) ru.he3hauka.toolvisuals.client.music.ui.MusicHud.draw(graphics,palette);
        if (HudPlacement.show(INVENTORY_HUD)) InventoryHud.draw(graphics, palette);
        if (HudPlacement.show(INFO_HUD)) {
            float scale = config.number(HUD_SCALE) / client.getWindow().getGuiScale();
            graphics.pose().pushMatrix(); graphics.pose().scale(scale, scale);
            ru.he3hauka.toolvisuals.client.hud.info.InfoHudPanel.draw(graphics,client,config,palette);
            if (config.flag(HUD_MODULES)) {
                int right = Math.round(graphics.guiWidth() / scale) - 12, y = 12;
                var active = modules(graphics);
                int maximum = Math.max(1, (int)((graphics.guiHeight() / scale - 24 + MODULE_GAP) / (MODULE_HEIGHT + MODULE_GAP)));
                var layout=ModuleListLimit.layout(active.size(),(int)config.value(HUD_MODULE_LIMIT),maximum);
                int shown=layout.shown();
                float listWidth = 1;
                for (int i = 0; i < shown; i++) listWidth = Math.max(listWidth, active.get(i).width);
                if (shown < active.size()) listWidth = Math.max(listWidth, UiPaint.textWidth(graphics, "+" + (active.size() - shown), MODULE_TEXT_SCALE) + 16);
                int lines = shown + (shown < active.size() ? 1 : 0);
                HudPlacement.place(graphics, HudElement.MODULES, right - listWidth, y, listWidth, Math.max(MODULE_HEIGHT, lines * (MODULE_HEIGHT + MODULE_GAP) - MODULE_GAP));
                for (int i = 0; i < shown; i++) {
                    var module = active.get(i);
                    moduleChip(graphics, module.label, module.width, right, y, palette, false);
                    y += MODULE_HEIGHT + MODULE_GAP;
                }
                if (shown < active.size()) {
                    String more = "+" + (active.size() - shown);
                    moduleChip(graphics, more, UiPaint.textWidth(graphics, more, MODULE_TEXT_SCALE) + 16, right, y, palette, true);
                }
                graphics.pose().popMatrix();
            }
            graphics.pose().popMatrix();
        }
        if (HudPlacement.show(ARMOR_HUD)) ArmorHud.draw(graphics, palette);
    }

    private static List<ModuleChip> modules(GuiGraphicsExtractor graphics) {
        var config = ToolVisualsClient.CONFIG;
        float density = UiSprites.pixelsPerUnit(graphics);
        String user = Minecraft.getInstance().getUser().getName();
        if (!Optimizations.enabled(OPT_FAST_HUD) || moduleRevision != config.revision()
                || moduleDensity != density || !moduleUser.equals(user)) {
            var active = java.util.Arrays.stream(VisualModule.values()).filter(config::active).toList();
            var widths = new java.util.EnumMap<VisualModule, Float>(VisualModule.class);
            for (var module : active) widths.put(module, UiPaint.textWidth(graphics, module.title, MODULE_TEXT_SCALE));
            moduleChips = ModuleListOrder.sort(active, (int)config.value(HUD_MODULE_ORDER), widths::get).stream()
                    .map(module -> new ModuleChip(NameProtect.text(module.title), widths.get(module) + 16)).toList();
            moduleRevision = config.revision(); moduleDensity = density; moduleUser = user;
        }
        return moduleChips;
    }

    private static void moduleChip(GuiGraphicsExtractor graphics, String label, float width, float right, float y, UiPalette palette, boolean overflow) {
        UiPaint.rounded(graphics, right - width, y, width, MODULE_HEIGHT, 4, palette.hudPanel);
        UiPaint.baseline(graphics, label, right - width + 8, UiPaint.textBaseline(graphics, y, MODULE_HEIGHT, MODULE_TEXT_SCALE),
                MODULE_TEXT_SCALE, overflow ? palette.muted : UiPalette.mix(palette.accentText, palette.text, .4f), 1);
    }

    public static void crosshair(GuiGraphicsExtractor graphics) {
        var client = Minecraft.getInstance();
        if (client.player == null || !client.options.getCameraType().isFirstPerson() || client.player.isSpectator()
                || client.gui.hud.isHidden() || client.gui.screen() != null) return;
        var config = ToolVisualsClient.CONFIG;
         
        float inverse = 1f / client.getWindow().getGuiScale();
        graphics.pose().pushMatrix(); graphics.pose().scale(inverse, inverse);
        float x = graphics.guiWidth() / inverse / 2, y = graphics.guiHeight() / inverse / 2;
        CrosshairPainter.draw(graphics, x, y, client.player.getAttackStrengthScale(0), 1);
        graphics.pose().popMatrix();
    }
}
