package com.viaversion.viafabricplus.injection.mixin.features.networking.config_state;

import net.minecraft.network.handler.NetworkStateTransitions;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.ChannelConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetworkStateTransitions.class)
public abstract class MixinNetworkStateTransitions {

    @WrapWithCondition(method = "method_56353", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelConfig;setAutoRead(Z)Lio/netty/channel/ChannelConfig;", remap = false))
    private static boolean dontChangeAutoRead(ChannelConfig instance, boolean b) {
        return ProtocolTranslator.getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_20_5);
    }

}
