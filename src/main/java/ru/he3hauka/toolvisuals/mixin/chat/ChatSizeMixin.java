package ru.he3hauka.toolvisuals.mixin.chat;

import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.chat.ChatResizer;

@Mixin(ChatComponent.class)
public abstract class ChatSizeMixin {
    @Inject(method="getWidth()I",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$customWidth(CallbackInfoReturnable<Integer> callback){callback.setReturnValue(ChatResizer.width(callback.getReturnValueI()));}
    @Inject(method="getHeight()I",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$customHeight(CallbackInfoReturnable<Integer> callback){callback.setReturnValue(ChatResizer.height(callback.getReturnValueI()));}
}
