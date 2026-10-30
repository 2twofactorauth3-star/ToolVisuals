package com.viaversion.viafabricplus.injection.mixin.features.item.negative_item_count;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import com.viaversion.viafabricplus.features.item.negative_item_count.NegativeItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {

    @Redirect(method = "renderItemCount", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"))
    private int handleNegativeItemCount(ItemStack instance) {
        return NegativeItemUtil.getCount(instance);
    }

    @Redirect(method = "renderItemCount", at = @At(value = "INVOKE", target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;", remap = false))
    private String makeTextRed(int count) {
        if (count <= 0) {
            return ChatFormatting.RED.toString() + count;
        } else {
            return String.valueOf(count);
        }
    }

}
