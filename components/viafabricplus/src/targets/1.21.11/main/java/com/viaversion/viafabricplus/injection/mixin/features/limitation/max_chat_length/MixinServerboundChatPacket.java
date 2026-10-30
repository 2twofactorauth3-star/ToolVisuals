package com.viaversion.viafabricplus.injection.mixin.features.limitation.max_chat_length;

import net.minecraft.network.protocol.game.ServerboundChatPacket;

import com.viaversion.viafabricplus.features.limitation.max_chat_length.MaxChatLength;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerboundChatPacket.class)
public abstract class MixinServerboundChatPacket {

    @ModifyConstant(method = "write", constant = @Constant(intValue = 256))
    private int modifyChatLength(int maxLength) {
        return MaxChatLength.getChatLength();
    }

}
