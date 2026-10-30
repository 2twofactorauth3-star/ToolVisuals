package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.KnowledgeBookItem;
import net.minecraft.util.Hand;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KnowledgeBookItem.class)
public abstract class MixinKnowledgeBookItem {

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrementUnlessCreative(ILnet/minecraft/entity/LivingEntity;)V"))
    private void removeFullStack(ItemStack instance, int amount, LivingEntity entity, @Local(argsOnly = true) Hand hand) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5)) {
            if (!entity.isInCreativeMode()) {
                entity.setStackInHand(hand, ItemStack.EMPTY);
            }
        } else {
            instance.decrementUnlessCreative(amount, entity);
        }
    }

}
