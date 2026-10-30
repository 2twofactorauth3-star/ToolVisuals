package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnimalEntity.class)
public abstract class MixinAnimalEntity {

    @Redirect(method = "interactMob", at = @At(value = "FIELD", target = "Lnet/minecraft/util/ActionResult;SUCCESS_SERVER:Lnet/minecraft/util/ActionResult$Success;"))
    private ActionResult.Success swingHand() {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.SUCCESS_SERVER;
        }
    }

    @Redirect(method = "interactMob", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z"))
    private boolean changeIsClientCondition(World instance) {
        return instance.isClient && ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_15);
    }

}
