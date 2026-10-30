package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.Creeper;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Creeper.class)
public abstract class MixinCreeper {

    @Redirect(method = "mobInteract", at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;FIRECHARGE_USE:Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent changeSound() {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_1)) {
            return SoundEvents.FLINTANDSTEEL_USE;
        } else {
            return SoundEvents.FIRECHARGE_USE;
        }
    }

}
