package com.viaversion.viafabricplus.injection.mixin.features.run_command_action;

import net.minecraft.client.multiplayer.ClientPacketListener;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Shadow
    protected abstract ClientPacketListener.CommandCheckResult verifyCommand(final String string);

    @Redirect(method = "sendUnattendedCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;verifyCommand(Ljava/lang/String;)Lnet/minecraft/client/multiplayer/ClientPacketListener$CommandCheckResult;"))
    private ClientPacketListener.CommandCheckResult dontOpenConfirmationScreens(ClientPacketListener instance, String string) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_5)) {
            final ClientPacketListener.CommandCheckResult result = verifyCommand(string);
            return result == ClientPacketListener.CommandCheckResult.PARSE_ERRORS || result == ClientPacketListener.CommandCheckResult.PERMISSIONS_REQUIRED ? ClientPacketListener.CommandCheckResult.NO_ISSUES : result;
        } else {
            return verifyCommand(string);
        }
    }

}
