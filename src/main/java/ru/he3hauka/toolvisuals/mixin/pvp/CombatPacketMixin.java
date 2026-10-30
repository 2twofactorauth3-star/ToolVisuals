package ru.he3hauka.toolvisuals.mixin.pvp;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.pvp.PvPSave;

@Mixin(ClientPacketListener.class)
public abstract class CombatPacketMixin {
    @Inject(method="handleEntityEvent",at=@At("TAIL"))
    private void toolvisuals$totemEvent(ClientboundEntityEventPacket packet,CallbackInfo ci){if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;ru.he3hauka.toolvisuals.client.pvp.TotemCounter.event(packet);ru.he3hauka.toolvisuals.client.animation.death.DeathAscension.event(packet);}
    @Inject(method="handleDamageEvent",at=@At("TAIL"))
    private void toolvisuals$combatDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;
        PvPSave.damage(packet);
    }
    @Inject(method="handleDamageEvent",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;handleDamageEvent(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void toolvisuals$prepareHitSound(ClientboundDamageEventPacket packet,CallbackInfo ci) {
        if(ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())return;
        ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.damage(packet);
    }
}
