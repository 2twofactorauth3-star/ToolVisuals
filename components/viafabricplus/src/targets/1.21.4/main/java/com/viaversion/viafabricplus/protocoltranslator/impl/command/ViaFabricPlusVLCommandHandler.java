package com.viaversion.viafabricplus.protocoltranslator.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.viaversion.viafabricplus.protocoltranslator.impl.command.classic.ListExtensionsCommand;
import com.viaversion.viafabricplus.protocoltranslator.impl.command.classic.SetTimeCommand;
import com.viaversion.vialoader.impl.viaversion.VLCommandHandler;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public final class ViaFabricPlusVLCommandHandler extends VLCommandHandler {

    public ViaFabricPlusVLCommandHandler() {
        super();

        this.removeSubCommand("list");
        this.removeSubCommand("player");
        this.removeSubCommand("pps");
        this.removeSubCommand("dump");

        this.registerSubCommand(new ListExtensionsCommand());
        this.registerSubCommand(new SetTimeCommand());
    }

    public int execute(final CommandContext<FabricClientCommandSource> ctx) {
        String[] args = new String[0];
        try {
            args = StringArgumentType.getString(ctx, "args").split(" ");
        } catch (IllegalArgumentException ignored) {
        }
        onCommand(new ViaFabricPlusViaCommandSender(ctx.getSource()), args);
        return 1;
    }

    public CompletableFuture<Suggestions> suggestion(CommandContext<FabricClientCommandSource> ctx, SuggestionsBuilder builder) {
        String[] args;
        try {
            args = StringArgumentType.getString(ctx, "args").split(" ", -1);
        } catch (IllegalArgumentException ignored) {
            args = new String[]{""};
        }
        final String[] pref = args.clone();
        pref[pref.length - 1] = "";

        final String prefix = String.join(" ", pref);
        onTabComplete(new ViaFabricPlusViaCommandSender(ctx.getSource()), args).stream().map(it -> {
            final SuggestionsBuilder b = new SuggestionsBuilder(builder.getInput(), prefix.length() + builder.getStart());
            b.suggest(it);
            return b;
        }).forEach(builder::add);
        return builder.buildFuture();
    }

}
