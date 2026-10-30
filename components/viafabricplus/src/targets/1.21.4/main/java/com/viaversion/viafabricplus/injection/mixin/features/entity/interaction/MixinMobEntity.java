package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity {

    @Shadow
    protected abstract ActionResult interactWithItem(PlayerEntity player, Hand hand);

    @Redirect(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;interactWithItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult moveItemInteractionsAfterLeashing(MobEntity instance, PlayerEntity player, Hand hand) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5)) {
            return ActionResult.FAIL;
        } else {
            return interactWithItem(player, hand);
        }
    }

    @Inject(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"), cancellable = true)
    private void moveItemInteractionsAfterLeashing(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5)) {
            final ActionResult result = interactWithItem(player, hand);
            if (result.isAccepted()) {
                cir.setReturnValue(result);
            }
        }
    }

}
