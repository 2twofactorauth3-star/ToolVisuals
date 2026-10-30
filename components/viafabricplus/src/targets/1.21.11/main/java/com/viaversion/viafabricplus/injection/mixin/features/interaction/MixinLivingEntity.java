package com.viaversion.viafabricplus.injection.mixin.features.interaction;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Redirect(method = "updatingUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean replaceItemStackEqualsCheck(ItemStack left, ItemStack right) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_3)) {
            return left == right;
        } else {
            return ItemStack.isSameItem(left, right);
        }
    }

    @Inject(method = "getEquipmentSlotForItem", at = @At("HEAD"), cancellable = true)
    private void removeShieldSlotPreference(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_9_3) && stack.is(Items.SHIELD)) {
            cir.setReturnValue(EquipmentSlot.MAINHAND);
        }
    }

}
