package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.level.Level;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderpearlItem.class)
public abstract class MixinEnderpearlItem {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void removeCreativeModeEnderPearl(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8) && user.getAbilities().instabuild) {
            ci.setReturnValue(InteractionResult.PASS);
        }
    }

}
