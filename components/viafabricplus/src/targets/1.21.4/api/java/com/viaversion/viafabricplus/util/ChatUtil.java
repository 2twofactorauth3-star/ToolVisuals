package com.viaversion.viafabricplus.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;


public final class ChatUtil {

    public static final String PREFIX = Formatting.WHITE + "[" + Formatting.GOLD + "ViaFabricPlus" + Formatting.WHITE + "]";
    public static final Text PREFIX_TEXT = Text.literal("[").formatted(Formatting.WHITE).append(Text.literal("ViaFabricPlus").formatted(Formatting.GOLD)).append("]");

    public static Text prefixText(final String message) {
        return prefixText(Text.of(message));
    }

    public static Text prefixText(final Text message) {
        return Text.empty().append(PREFIX_TEXT).append(" ").append(message);
    }

    public static void sendPrefixedMessage(final Text message) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(prefixText(message));
    }

    public static String uncoverTranslationKey(final Text text) {
        return ((TranslatableTextContent) text.getContent()).getKey();
    }

}
