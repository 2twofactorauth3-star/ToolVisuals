package com.viaversion.viafabricplus.injection.mixin.features.run_command_action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen {

    @Final
    @Shadow
    @Nullable
    protected Minecraft minecraft;

    @Inject(method = "clickCommandAction", at = @At("HEAD"), cancellable = true)
    private static void changeCommandHandling(LocalPlayer player, String command, Screen screenAfterRun, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_21_4)) {
            return;
        }

        if (!command.startsWith("/")) {
            ci.cancel();
            if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19)) {
                player.connection.sendChat(command);
            }
        }
    }

}
