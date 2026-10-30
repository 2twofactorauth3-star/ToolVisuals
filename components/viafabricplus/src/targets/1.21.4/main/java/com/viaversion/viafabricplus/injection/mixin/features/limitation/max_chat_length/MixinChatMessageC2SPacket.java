package com.viaversion.viafabricplus.injection.mixin.features.limitation.max_chat_length;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import com.viaversion.viafabricplus.features.limitation.max_chat_length.MaxChatLength;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ChatMessageC2SPacket.class)
public abstract class MixinChatMessageC2SPacket {

    @ModifyConstant(method = "write", constant = @Constant(intValue = 256))
    private int modifyChatLength(int maxLength) {
        return MaxChatLength.getChatLength();
    }

}
