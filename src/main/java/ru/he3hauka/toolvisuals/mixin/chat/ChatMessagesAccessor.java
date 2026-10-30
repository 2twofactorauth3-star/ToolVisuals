package ru.he3hauka.toolvisuals.mixin.chat;

import java.util.List;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatComponent.class)
public interface ChatMessagesAccessor {
    @Accessor("allMessages") List<GuiMessage> toolvisuals$messages();
}
