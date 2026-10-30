package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class MixinBowItem {

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    private void makeInstantUsable_Time(CallbackInfoReturnable<Integer> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.b1_7tob1_7_3)) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    private void makeInstantUsable_Action(CallbackInfoReturnable<UseAction> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.b1_7tob1_7_3)) {
            cir.setReturnValue(UseAction.NONE);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void makeInstantUsable(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.b1_7tob1_7_3)) {
            final ItemStack arrowStack = user.getProjectileType(user.getStackInHand(hand));
            if (arrowStack.isEmpty()) {
                cir.setReturnValue(ActionResult.FAIL);
            } else {
                arrowStack.decrement(1);
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }

}
