package com.viaversion.viafabricplus.visuals.injection.mixin.secure_chat_warning;

import net.minecraft.client.network.ClientPlayNetworkHandler;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow
    protected abstract boolean isSecureChatEnforced();

    @Redirect(method = "onGameJoin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;isSecureChatEnforced()Z"))
    private boolean removeSecureChatWarning(ClientPlayNetworkHandler instance) {
        return isSecureChatEnforced() || VisualSettings.INSTANCE.disableSecureChatWarning.isEnabled();
    }

}
