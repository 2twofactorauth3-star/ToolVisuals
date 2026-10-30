package com.viaversion.viafabricplus.injection.mixin.features.swinging;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {

    @WrapWithCondition(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"))
    private boolean dontSwingHand(PlayerEntity instance, Hand hand) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_15_2);
    }

}
