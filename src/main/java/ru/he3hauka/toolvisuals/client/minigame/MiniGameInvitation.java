package ru.he3hauka.toolvisuals.client.minigame;

import java.util.UUID;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.arguments.UuidArgument;

import ru.he3hauka.toolvisuals.client.chat.message.ChatColors;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.Message;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.*;

public final class MiniGameInvitation {
    private static final String COMMAND = "toolvisuals_minigame";
    private MiniGameInvitation() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = literal(COMMAND);
        for (boolean accept : new boolean[]{true, false}) root.then(literal(action(accept)).then(argument("invitation", UuidArgument.uuid())
                .executes(context -> MiniGamesClient.answer(context.getArgument("invitation", UUID.class), accept) ? 1 : 0)));
        dispatcher.register(root);
    }

    public static Component message(Message invitation, String sender) {
        return ChatMessages.translated("minigames.invitation", sender, UiI18n.tr("toolvisuals.minigames.game." + invitation.kind().key))
                .append("\n").append(button(invitation.room(), true)).append("  ").append(button(invitation.room(), false))
                .append(UiI18n.tr("toolvisuals.minigames.invitation_expiry", invitation.seconds()).withColor(ChatColors.MUTED));
    }

    private static Component button(UUID id, boolean accept) {
        return UiI18n.tr("toolvisuals.minigames." + action(accept)).withStyle(style -> style.withColor(accept ? ChatColors.SUCCESS : ChatColors.ERROR)
                .withClickEvent(new ClickEvent.RunCommand("/" + COMMAND + " " + action(accept) + " " + id))
                .withHoverEvent(new HoverEvent.ShowText(UiI18n.tr("toolvisuals.minigames." + action(accept) + "_hint").withColor(ChatColors.TEXT))));
    }
    private static String action(boolean accept) { return accept ? "accept" : "decline"; }
}
