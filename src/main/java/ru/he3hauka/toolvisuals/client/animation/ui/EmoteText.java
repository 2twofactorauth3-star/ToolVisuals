package ru.he3hauka.toolvisuals.client.animation.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import ru.he3hauka.toolvisuals.animation.EmoteGroup;
import ru.he3hauka.toolvisuals.animation.PairEmote;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class EmoteText {
    private EmoteText() {}

    public static int color(EmoteGroup group) {
        return switch (group) {
            case SOCIAL -> 0x77caff;
            case JOY -> 0xffc776;
            case AFFECTION -> 0xff87b7;
            case MOOD -> 0x7cebc5;
            case TRICKS -> 0xb59aff;
        };
    }

    public static MutableComponent pair(PairEmote emote) {
        return UiI18n.tr("toolvisuals.pairs.chat_emote", UiI18n.tr("toolvisuals.pairs." + emote.key))
                .withColor(color(emote.animation.group));
    }
}
