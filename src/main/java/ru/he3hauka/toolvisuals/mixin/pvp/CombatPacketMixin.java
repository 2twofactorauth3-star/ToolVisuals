package ru.he3hauka.toolvisuals.mixin.pvp;

import net.minecraft.client.multiplayer.ClientPacketListener;
import ru.he3hauka.toolvisuals.client.chat.reply.AutoGG;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.animation.death.DeathAscension;
import ru.he3hauka.toolvisuals.client.potion.tracker.BuffTracker;
import ru.he3hauka.toolvisuals.client.pvp.PvPSave;
import ru.he3hauka.toolvisuals.client.pvp.TotemCounter;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;
import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer;

@Mixin(ClientPacketListener.class)
public abstract class CombatPacketMixin {
    @Inject(method="handleEntityEvent",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;handleEntityEvent(B)V"))
    private void toolvisuals$potionEvent(ClientboundEntityEventPacket packet,CallbackInfo ci){BuffTracker.event(packet);}
    @Inject(method="handleEntityEvent",at=@At("TAIL"))
    private void toolvisuals$totemEvent(ClientboundEntityEventPacket packet,CallbackInfo ci){if(ReplayScope.active())return;TotemCounter.event(packet);DeathAscension.event(packet);AutoGG.event(packet);}
    @Inject(method="handleDamageEvent",at=@At("TAIL"))
    private void toolvisuals$combatDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        if(ReplayScope.active())return;
        PvPSave.damage(packet); AutoGG.damage(packet);
    }
    @Inject(method="handleDamageEvent",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;handleDamageEvent(Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void toolvisuals$prepareHitSound(ClientboundDamageEventPacket packet,CallbackInfo ci) {
        if(ReplayScope.active())return;
        SoundCustomizer.damage(packet);
    }
}
