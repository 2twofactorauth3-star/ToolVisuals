package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
public abstract class MixinBundleItem {

    @Inject(method = "use", at = @At("RETURN"), cancellable = true)
    private void dontSwing(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            final ItemStack itemStack = user.getItemInHand(hand);
            final BundleContents component = itemStack.get(DataComponents.BUNDLE_CONTENTS);
            if (component == null || component.isEmpty()) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_2)) {
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

}
