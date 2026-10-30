package com.viaversion.viafabricplus.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;


public final class ItemUtil {

    public static NbtCompound getTagOrNull(final ItemStack stack) {
        final NbtComponent tag = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (tag != null) {
            return tag.copyNbt();
        } else {
            return null;
        }
    }

}
