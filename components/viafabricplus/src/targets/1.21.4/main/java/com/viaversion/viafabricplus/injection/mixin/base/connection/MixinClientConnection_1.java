package com.viaversion.viafabricplus.injection.mixin.base.connection;

import net.minecraft.network.ClientConnection;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import io.netty.channel.Channel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.ClientConnection$1")
public abstract class MixinClientConnection_1 {

    @Final
    @Shadow
    ClientConnection field_11663;

    @Inject(method = "initChannel", at = @At("RETURN"))
    private void injectViaIntoPipeline(Channel channel, CallbackInfo ci) {
        ProtocolTranslator.injectViaPipeline(this.field_11663, channel);
    }

}
