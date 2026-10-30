package ru.he3hauka.toolvisuals.mixin.player;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.hud.cooldown.Cooldowns;
import ru.he3hauka.toolvisuals.client.items.ChangeHand;
import ru.he3hauka.toolvisuals.client.player.FakePlayer;
import ru.he3hauka.toolvisuals.client.pvp.PvPSave;
import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientActionsMixin {
    @Inject(method="useItem",at=@At("HEAD"))
    private void toolvisuals$rememberUsedItem(Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir) {
        Cooldowns.remember(player.getCooldowns(),player.getItemInHand(hand));
    }
    @Inject(method="attack", at=@At("HEAD"), cancellable=true)
    private void toolvisuals$fakeAttack(Player player, Entity target, CallbackInfo ci) {
        if (FakePlayer.attack(player,target)) { ChangeHand.attacked(target); ru.he3hauka.toolvisuals.client.hud.health.HealthIndicator.attacked(player,target); ci.cancel(); }
        else SoundCustomizer.attacking(player,target);
    }
    @Inject(method="attack", at=@At("TAIL"))
    private void toolvisuals$attacked(Player player, Entity target, CallbackInfo ci) { ChangeHand.attacked(target); PvPSave.attacked(target); ru.he3hauka.toolvisuals.client.optimization.crystal.CrystalOptimizer.attacked(player,target); ru.he3hauka.toolvisuals.client.hud.health.HealthIndicator.attacked(player,target); }
    @Inject(method="interact", at=@At("HEAD"), cancellable=true)
    private void toolvisuals$fakeInteraction(Player player, Entity target, EntityHitResult hit, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (FakePlayer.owns(target)) cir.setReturnValue(InteractionResult.PASS);
    }
}
