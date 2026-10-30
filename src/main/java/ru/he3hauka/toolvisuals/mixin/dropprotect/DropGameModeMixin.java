package ru.he3hauka.toolvisuals.mixin.dropprotect;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.items.dropprotect.DropProtect;

@Mixin(MultiPlayerGameMode.class)
public abstract class DropGameModeMixin {
    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$protectDrop(LocalPlayer player, boolean entireStack, CallbackInfo ci) {
        if (DropProtect.world(player, entireStack)) ci.cancel();
    }
}
