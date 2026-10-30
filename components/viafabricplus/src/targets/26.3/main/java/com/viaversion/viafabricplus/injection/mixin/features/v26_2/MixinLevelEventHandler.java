package com.viaversion.viafabricplus.injection.mixin.features.v26_2;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelEventHandler.class)
public abstract class MixinLevelEventHandler {

    @Shadow
    public abstract void levelEvent(int eventType, BlockPos pos, int data);

    @Inject(method = "levelEvent", at = @At("HEAD"))
    private void playPotionSplashSound(int eventType, BlockPos pos, int data, CallbackInfo ci) {

        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            if (eventType == LevelEvent.PARTICLES_SPELL_POTION_SPLASH) {
                this.levelEvent(LevelEvent.SOUND_SPELL_POTION_SPLASH, pos, data);
            } else if (eventType == LevelEvent.PARTICLES_INSTANT_POTION_SPLASH) {
                this.levelEvent(LevelEvent.SOUND_INSTANT_POTION_SPLASH, pos, data);
            }
        }
    }

}
