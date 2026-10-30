package com.viaversion.viafabricplus.injection.mixin.base.integration;

import net.minecraft.network.Connection;
import net.minecraft.network.HandlerNames;
import net.minecraft.network.protocol.Packet;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import com.viaversion.viaversion.platform.ViaChannelInitializer;
import io.netty.channel.ChannelHandlerContext;
import java.net.ConnectException;
import java.net.SocketException;
import io.netty.channel.SimpleChannelInboundHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class MixinConnection extends SimpleChannelInboundHandler<Packet<?>> {

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void printNetworkingErrors(ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
        if (DebugSettings.INSTANCE.printNetworkingErrorsToLogs.getValue()) {
            if (ex instanceof SocketException || ex instanceof ConnectException) {

                return;
            }
            ViaFabricPlusImpl.INSTANCE.getLogger().error("An exception occurred while handling a packet", ex);
        }
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {

        if (evt.getClass().getName().equals("me.steinborn.krypton.mod.shared.misc.KryptonPipelineEvent")) {
            if (evt.toString().equals("COMPRESSION_ENABLED")) {
                ViaChannelInitializer.reorderPipeline(ctx.pipeline(), HandlerNames.COMPRESS, HandlerNames.DECOMPRESS);
                ViaFabricPlusImpl.INSTANCE.getLogger().warn("ViaFabricPlus has detected that the Krypton mod is installed. Please note that Krypton is mostly snake oil on the client side, and it is not recommended to use it.");
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

}
