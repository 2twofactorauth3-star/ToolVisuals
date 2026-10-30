package com.viaversion.viafabricplus.injection.mixin.features.limitation.max_chat_length;

import net.minecraft.util.StringHelper;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viaversion.viafabricplus.features.limitation.max_chat_length.MaxChatLength;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StringHelper.class)
public abstract class MixinStringHelper {

    @ModifyExpressionValue(method = "truncateChat", at = @At(value = "CONSTANT", args = "intValue=256"))
    private static int modifyMaxChatLength(int constant) {
        return MaxChatLength.getChatLength();
    }

}
