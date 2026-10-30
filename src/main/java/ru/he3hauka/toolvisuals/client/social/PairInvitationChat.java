package ru.he3hauka.toolvisuals.client.social;

import java.util.UUID;
import java.util.function.BiPredicate;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import ru.he3hauka.toolvisuals.animation.social.SocialProtocol;
import ru.he3hauka.toolvisuals.client.animation.ui.EmoteText;
import ru.he3hauka.toolvisuals.client.chat.message.ChatColors;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class PairInvitationChat {
    private static final String COMMAND = "toolvisuals_pair";
    private PairInvitationChat() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, BiPredicate<UUID, Boolean> answer,
                                java.util.function.BooleanSupplier available) {
        var root = literal(COMMAND).requires(source -> available.getAsBoolean());
        for (boolean accept : new boolean[] {true, false}) root.then(literal(action(accept))
                .then(argument("invitation", UuidArgument.uuid()).executes(context -> answer.test(context.getArgument("invitation", UUID.class), accept) ? 1 : 0)));
        dispatcher.register(root);
    }

    public static Component message(SocialProtocol.Invitation invitation, String sender) {
        return ChatMessages.translated("pairs.received", sender, EmoteText.pair(invitation.emote()))
                .append("\n").append(button(invitation.id(), true)).append("  ").append(button(invitation.id(), false))
                .append(UiI18n.tr("toolvisuals.pairs.chat_expiry").withColor(ChatColors.MUTED));
    }

    private static Component button(UUID invitation, boolean accept) {
        return UiI18n.tr("toolvisuals.pairs.chat_" + action(accept)).withStyle(style -> style.withColor(accept ? ChatColors.SUCCESS : ChatColors.ERROR)
                .withClickEvent(new ClickEvent.RunCommand("/" + COMMAND + " " + action(accept) + " " + invitation))
                .withHoverEvent(new HoverEvent.ShowText(UiI18n.tr("toolvisuals.pairs." + action(accept) + "_hint").withColor(ChatColors.TEXT))));
    }

    private static String action(boolean accept) { return accept ? "accept" : "decline"; }
}
