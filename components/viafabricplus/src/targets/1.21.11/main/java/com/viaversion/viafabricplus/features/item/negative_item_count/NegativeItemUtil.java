package com.viaversion.viafabricplus.features.item.negative_item_count;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.viaversion.viafabricplus.util.ItemUtil;
import com.viaversion.viaversion.protocols.v1_10to1_11.Protocol1_10To1_11;

public final class NegativeItemUtil {

    public static int getCount(final ItemStack stack) {
        final CompoundTag tag = ItemUtil.getTagOrNull(stack);
        if (tag != null) {
            return tag.getIntOr(ItemUtil.vvNbtName(Protocol1_10To1_11.class), stack.getCount());
        } else {
            return stack.getCount();
        }
    }

}
