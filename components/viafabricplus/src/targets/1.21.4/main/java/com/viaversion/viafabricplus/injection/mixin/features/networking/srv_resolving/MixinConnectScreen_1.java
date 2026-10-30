package com.viaversion.viafabricplus.injection.mixin.features.networking.srv_resolving;

import net.minecraft.client.network.ServerAddress;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetSocketAddress;

@Mixin(targets = "net.minecraft.client.gui.screen.multiplayer.ConnectScreen$1")
public abstract class MixinConnectScreen_1 {

    @Final
    @Shadow
    ServerAddress field_33737;

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/net/InetSocketAddress;getHostName()Ljava/lang/String;", remap = false))
    private String getRealAddress(InetSocketAddress instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_17)) {
            return field_33737.getAddress();
        } else {
            return instance.getHostName();
        }
    }

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Ljava/net/InetSocketAddress;getPort()I", remap = false))
    private int getRealPort(InetSocketAddress instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_17)) {
            return field_33737.getPort();
        } else {
            return instance.getPort();
        }
    }

}
