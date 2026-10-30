package ru.he3hauka.toolvisuals.client.items.shulkertooltip;

import java.util.Optional;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.SHULKER_TOOLTIP;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ShulkerTooltips {
    private record Cached(ItemContainerContents contents, int color, ShulkerPreview preview, ShulkerTooltipComponent renderer) {}
    private static volatile Cached cache;

    private ShulkerTooltips() {}

    public static void initialize() {
        ClientTooltipComponentCallback.EVENT.register(ShulkerTooltips::component);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
    }

    public static Optional<ShulkerPreview> image(ItemStack stack) {
        var config = ToolVisualsClient.CONFIG;
        return image(config, stack, !config.flag(SHULKER_SHIFT) || Minecraft.getInstance().hasShiftDown());
    }

    public static Optional<ShulkerPreview> image(ClientConfig config, ItemStack stack, boolean shift) {
        if (!config.active(SHULKER_TOOLTIP)) { clear(); return Optional.empty(); }
        if (stack.isEmpty() || config.flag(SHULKER_SHIFT) && !shift || !(stack.getItem() instanceof BlockItem item)
                || !(item.getBlock() instanceof ShulkerBoxBlock block) || stack.has(DataComponents.CONTAINER_LOOT)) return Optional.empty();
        var display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (display.hideTooltip() || !display.shows(DataComponents.CONTAINER)) return Optional.empty();
        var contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        int color = config.flag(SHULKER_COLOR) ? block.getColor() == null ? 0xff9e80f5 : 0xff000000 | block.getColor().getTextureDiffuseColor() : 0;
        var current = cache;
        if (current == null || current.contents != contents || current.color != color) {
            var preview = new ShulkerPreview(contents, color);
            cache = current = new Cached(contents, color, preview, new ShulkerTooltipComponent(preview));
        }
        return current.preview.occupied == 0 && !config.flag(SHULKER_EMPTY) ? Optional.empty() : Optional.of(current.preview);
    }

    public static ClientTooltipComponent component(TooltipComponent component) {
        if (!(component instanceof ShulkerPreview preview)) return null;
        var current = cache;
        return current != null && current.preview == preview ? current.renderer : new ShulkerTooltipComponent(preview);
    }

    public static void clear() { cache = null; }
}
