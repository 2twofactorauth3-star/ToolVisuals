package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Animal.class)
public abstract class MixinAnimal {

    @Redirect(method = "mobInteract", at = @At(value = "FIELD", target = "Lnet/minecraft/world/InteractionResult;SUCCESS_SERVER:Lnet/minecraft/world/InteractionResult$Success;"))
    private InteractionResult.Success swingHand() {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.SUCCESS_SERVER;
        }
    }

    @Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
    private boolean changeIsClientCondition(Level instance) {
        return instance.isClientSide() && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_15);
    }

}
