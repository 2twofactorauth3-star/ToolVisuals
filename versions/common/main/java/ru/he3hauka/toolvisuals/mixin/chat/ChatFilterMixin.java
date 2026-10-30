package ru.he3hauka.toolvisuals.mixin.chat;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.utility.chat.ChatTabs;

@Mixin(ChatComponent.class)
public abstract class ChatFilterMixin {
    @Inject(method="addMessageToDisplayQueue",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$visible(GuiMessage message,CallbackInfo callback){if(!ChatTabs.visible(message))callback.cancel();}
}
