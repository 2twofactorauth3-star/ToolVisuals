package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EquippableComponent.class)
public abstract class MixinEquippableComponent {

    @Shadow
    @Final
    private EquipmentSlot slot;

    @Redirect(method = "equip", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isCreative()Z"))
    private boolean removeCreativeCondition(PlayerEntity instance) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_20) && instance.isCreative();
    }

    @Inject(method = "equip", at = @At("HEAD"), cancellable = true)
    private void cancelArmorSwap(ItemStack stack, PlayerEntity player, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_3)) {
            final ItemStack targetItem = player.getEquippedStack(this.slot);
            if (!targetItem.isEmpty()) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_4_6tor1_4_7)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

}
