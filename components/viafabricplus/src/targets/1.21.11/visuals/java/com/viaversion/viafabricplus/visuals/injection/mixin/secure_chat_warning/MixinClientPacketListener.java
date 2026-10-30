package com.viaversion.viafabricplus.visuals.injection.mixin.secure_chat_warning;

import net.minecraft.client.multiplayer.ClientPacketListener;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Shadow
    protected abstract boolean enforcesSecureChat();

    @Redirect(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;enforcesSecureChat()Z"))
    private boolean removeSecureChatWarning(ClientPacketListener instance) {
        return enforcesSecureChat() || VisualSettings.INSTANCE.disableSecureChatWarning.isEnabled();
    }

}
