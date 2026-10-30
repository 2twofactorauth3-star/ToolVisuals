package com.viaversion.viafabricplus.injection.mixin.features.v1_9_3.network;

import com.viaversion.viafabricplus.ViaFabricPlus;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerboundChatPacket.class)
public abstract class MixinServerboundChatPacket {

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/ByteBufCodecs;stringUtf8(I)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<ByteBuf, String> modifyChatLength(int maxStringLength) {
        return StreamCodec.of(
            (output, value) -> Utf8String.write(output, value, ViaFabricPlus.api().limitations().maxChatLength()),
            input -> Utf8String.read(input, ViaFabricPlus.api().limitations().maxChatLength())
        );
    }

}
