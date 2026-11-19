package ru.he3hauka.toolvisuals.mixin.optimization.network;

import io.netty.channel.ChannelOutboundHandler;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.optimization.network.NetworkOptimizations;
import ru.he3hauka.toolvisuals.client.optimization.network.PacketFrameEncoder;

@Mixin(Connection.class)
public abstract class FrameEncoderMixin {
    @Inject(method="createFrameEncoder",at=@At("RETURN"),cancellable=true)
    private static void toolvisuals$encoder(boolean local,CallbackInfoReturnable<ChannelOutboundHandler> callback){if(!local&&!NetworkOptimizations.external())callback.setReturnValue(PacketFrameEncoder.INSTANCE);}
}
