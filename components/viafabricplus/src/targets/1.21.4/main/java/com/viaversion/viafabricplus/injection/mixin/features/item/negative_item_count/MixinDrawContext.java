package com.viaversion.viafabricplus.injection.mixin.features.item.negative_item_count;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;

import com.viaversion.viafabricplus.features.item.negative_item_count.NegativeItemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DrawContext.class)
public abstract class MixinDrawContext {

    @Redirect(method = "drawStackCount", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"))
    private int handleNegativeItemCount(ItemStack instance) {
        return NegativeItemUtil.getCount(instance);
    }

    @Redirect(method = "drawStackCount", at = @At(value = "INVOKE", target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;", remap = false))
    private String makeTextRed(int count) {
        if (count <= 0) {
            return Formatting.RED.toString() + count;
        } else {
            return String.valueOf(count);
        }
    }

}
