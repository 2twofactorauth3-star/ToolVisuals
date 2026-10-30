package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderPearlItem.class)
public abstract class MixinEnderPearlItem {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void removeCreativeModeEnderPearl(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8) && user.getAbilities().creativeMode) {
            ci.setReturnValue(ActionResult.PASS);
        }
    }

}
