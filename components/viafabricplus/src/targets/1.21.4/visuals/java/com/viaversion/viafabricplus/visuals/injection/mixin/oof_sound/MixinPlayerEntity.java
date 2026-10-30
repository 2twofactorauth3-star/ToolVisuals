package com.viaversion.viafabricplus.visuals.injection.mixin.oof_sound;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {

    @Unique
    private static final SoundEvent viaFabricPlusVisuals$oof_hurt = SoundEvent.of(Identifier.of("viafabricplus-visuals", "oof.hurt"));

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void replaceSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        if (VisualSettings.INSTANCE.replaceHurtSoundWithOOFSound.isEnabled()) {
            cir.setReturnValue(viaFabricPlusVisuals$oof_hurt);
        }
    }

}
