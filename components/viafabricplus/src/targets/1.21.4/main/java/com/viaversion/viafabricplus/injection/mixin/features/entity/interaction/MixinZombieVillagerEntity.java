package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.util.ActionResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ZombieVillagerEntity.class)
public abstract class MixinZombieVillagerEntity {

    @Redirect(method = "interactMob", at = @At(value = "FIELD", target = "Lnet/minecraft/util/ActionResult;SUCCESS_SERVER:Lnet/minecraft/util/ActionResult$Success;"))
    private ActionResult.Success swingHand() {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21)) {
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.SUCCESS_SERVER;
        }
    }

}
