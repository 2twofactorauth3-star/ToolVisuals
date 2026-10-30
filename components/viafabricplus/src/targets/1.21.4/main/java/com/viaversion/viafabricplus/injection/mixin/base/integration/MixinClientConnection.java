package com.viaversion.viafabricplus.injection.mixin.base.integration;

import net.minecraft.network.ClientConnection;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.ConnectException;
import java.net.SocketException;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void printNetworkingErrors(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        if (DebugSettings.INSTANCE.printNetworkingErrorsToLogs.getValue()) {
            if (ex instanceof SocketException || ex instanceof ConnectException) {

                return;
            }
            ViaFabricPlusImpl.INSTANCE.logger().error("An exception occurred while handling a packet", ex);
        }
    }

}
