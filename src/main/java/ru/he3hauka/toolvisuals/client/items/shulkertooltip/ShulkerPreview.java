package ru.he3hauka.toolvisuals.client.items.shulkertooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class ShulkerPreview implements TooltipComponent {
    public static final int COLUMNS = 9, ROWS = 3, SLOTS = COLUMNS * ROWS;
    public final int color, occupied;
    private final NonNullList<ItemStack> items;

    public ShulkerPreview(ItemContainerContents contents, int color) {
        this.color = color;
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        contents.copyInto(items);
        int occupied = 0;
        for (var item : items) if (!item.isEmpty()) occupied++;
        this.occupied = occupied;
    }

    public ItemStack item(int slot) { return items.get(slot); }
}
