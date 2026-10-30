package com.viaversion.viafabricplus.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;


public final class ChatUtil {

    public static final String PREFIX = ChatFormatting.WHITE + "[" + ChatFormatting.GOLD + "ViaFabricPlus" + ChatFormatting.WHITE + "]";
    public static final Component PREFIX_TEXT = Component.literal("[").withStyle(ChatFormatting.WHITE).append(Component.literal("ViaFabricPlus").withStyle(ChatFormatting.GOLD)).append("]");

    public static Component prefixText(final String message) {
        return prefixText(Component.nullToEmpty(message));
    }

    public static Component prefixText(final Component message) {
        return Component.empty().append(PREFIX_TEXT).append(" ").append(message);
    }

    public static void sendPrefixedMessage(final Component message) {
        if (Minecraft.getInstance().isSameThread()) {
            Minecraft.getInstance().gui.getChat().addMessage(prefixText(message));
        } else {
            Minecraft.getInstance().execute(() -> sendPrefixedMessage(message));
        }
    }

    public static String uncoverTranslationKey(final Component text) {
        return ((TranslatableContents) text.getContents()).getKey();
    }

}
