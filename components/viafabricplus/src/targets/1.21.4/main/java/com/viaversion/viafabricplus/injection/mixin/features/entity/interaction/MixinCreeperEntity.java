package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreeperEntity.class)
public abstract class MixinCreeperEntity {

    @Redirect(method = "interactMob", at = @At(value = "FIELD", target = "Lnet/minecraft/sound/SoundEvents;ITEM_FIRECHARGE_USE:Lnet/minecraft/sound/SoundEvent;"))
    private SoundEvent changeSound() {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_1)) {
            return SoundEvents.ITEM_FLINTANDSTEEL_USE;
        } else {
            return SoundEvents.ITEM_FIRECHARGE_USE;
        }
    }

}
