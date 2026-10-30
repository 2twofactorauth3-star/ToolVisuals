package ru.he3hauka.toolvisuals.client.food;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import squeek.appleskin.api.event.HUDOverlayEvent;
import squeek.appleskin.client.HUDOverlayHandler;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;
import ru.he3hauka.toolvisuals.mixin.appleskin.AppleSkinOffsetsInvoker;

public final class FoodVisuals {
    private static final Identifier EMPTY_FOOD = Identifier.withDefaultNamespace("hud/food_empty");
    private static final int ROW_SPACING = 10;

    private FoodVisuals() {}
    public static boolean enabled(){return ToolVisualsClient.CONFIG.active(VisualModule.APPLE_SKIN);}
    public static boolean hud(VisualSetting setting,RenderOption bar){
        return enabled()&&ToolVisualsClient.CONFIG.flag(setting)&&!NoRender.hides(RenderOption.ALL_HUD)&&!NoRender.hides(bar);
    }
    public static boolean tooltips(){return enabled()&&ToolVisualsClient.CONFIG.flag(VisualSetting.APPLE_TOOLTIPS);}

    public static void foodOverlay(HUDOverlayEvent event, VisualSetting setting) {
        if (!hud(setting, RenderOption.FOOD)) {
            event.isCanceled = true;
            return;
        }
        event.y = rowY(event.y, Minecraft.getInstance().player);
    }

    public static void drawRow(GuiGraphicsExtractor graphics, Player player, int hungerY, int originX) {
        if (!separateRow() || player == null || !(hud(VisualSetting.APPLE_SATURATION, RenderOption.FOOD)
                || hud(VisualSetting.APPLE_EXHAUSTION, RenderOption.FOOD)
                || hud(VisualSetting.APPLE_HUNGER_PREVIEW, RenderOption.FOOD))) return;
        int y = rowY(hungerY, player);
        int tick = Minecraft.getInstance().gui.hud.getGuiTicks();
        var offsets = ((AppleSkinOffsetsInvoker)(Object)HUDOverlayHandler.INSTANCE.barOffsets).toolvisuals$foodBarOffsets(tick, player);
        for (var offset : offsets) {
            if (offset != null) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EMPTY_FOOD,
                    originX + offset.x, y + offset.y, 9, 9);
        }
    }

    private static int rowY(int hungerY, Player player) {
        if (!separateRow()) return hungerY;
        boolean airVisible = player != null && (player.isEyeInFluid(FluidTags.WATER)
                || player.getAirSupply() < player.getMaxAirSupply());
        return hungerY - ROW_SPACING * (airVisible ? 2 : 1);
    }

    private static boolean separateRow() {
        return ToolVisualsClient.CONFIG.number(VisualSetting.APPLE_LAYOUT) == 0;
    }
}
