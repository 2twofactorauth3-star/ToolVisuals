package ru.he3hauka.toolvisuals.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

public final class LocalSuggestions {
    private static CommandDispatcher<Object> cached;
    private static long revision = Long.MIN_VALUE;
    private LocalSuggestions() {}

    public static long policyRevision() { return ToolVisualsClient.CONFIG.serverPolicy.revision(); }
    public static CommandDispatcher<Object> dispatcher() {
        long current = policyRevision();
        if (cached == null || revision != current) {
            cached = create(feature -> ToolVisualsClient.CONFIG.serverPolicy.allows(feature), LocalSuggestions::worlds, LocalSuggestions::coordinates);
            revision = current;
        }
        return cached;
    }

    public static CommandDispatcher<Object> create(Predicate<String> available, Supplier<List<String>> worlds,
                                                   Supplier<List<String>> coordinates) {
        var dispatcher = new CommandDispatcher<Object>();
        for (String alias : LocalCommandLine.ROOTS) {
            var root = literal(alias.substring(1));
            for (var command : LocalCommand.values()) {
                if (command.featureId != null && !available.test(command.featureId)) continue;
                var node = literal(command.id).executes(context -> 1)
                        .requires(source -> command.featureId == null || available.test(command.featureId));
                switch (command) {
                    case LOG -> { node.then(literal("start").executes(context -> 1)); node.then(literal("stop").executes(context -> 1)); }
                    case PRESENTS -> node.then(literal("reset").executes(context -> 1));
                    case GPS -> {
                        node.then(literal("off").executes(context -> 1));
                        node.then(literal("list").executes(context -> 1));
                        var x = coordinate("x", 30_000_000, 0, coordinates);
                        var y = coordinate("y", 4096, 1, coordinates);
                        var z = coordinate("z", 30_000_000, 2, coordinates).executes(context -> 1);
                        z.then(RequiredArgumentBuilder.<Object, String>argument("world", StringArgumentType.greedyString())
                                .suggests((context, builder) -> suggest(worlds.get(), builder)).executes(context -> 1));
                        node.then(x.then(y.then(z)));
                    }
                    case AUCTION -> {
                        node.then(literal("cancel").executes(context -> 1));
                        node.then(literal("use").then(RequiredArgumentBuilder.<Object, String>argument("id", StringArgumentType.greedyString())
                                .executes(context -> 1)));
                        node.then(RequiredArgumentBuilder.<Object, String>argument("id", StringArgumentType.greedyString()).executes(context -> 1));
                    }
                    default -> {}
                }
                root.then(node);
            }
            dispatcher.register(root);
        }
        return dispatcher;
    }

    private static LiteralArgumentBuilder<Object> literal(String name) { return LiteralArgumentBuilder.literal(name); }

    private static RequiredArgumentBuilder<Object, Double> coordinate(String name, int limit, int index,
                                                                       Supplier<List<String>> coordinates) {
        return RequiredArgumentBuilder.<Object, Double>argument(name, DoubleArgumentType.doubleArg(-limit, limit))
                .suggests((context, builder) -> suggest(List.of(coordinates.get().get(index)), builder));
    }

    private static CompletableFuture<Suggestions> suggest(List<String> values, SuggestionsBuilder builder) {
        String prefix = builder.getRemainingLowerCase();
        for (String value : values) if (value.toLowerCase(Locale.ROOT).startsWith(prefix)) builder.suggest(value);
        return builder.buildFuture();
    }

    private static List<String> worlds() {
        var worlds = new java.util.LinkedHashSet<>(List.of("overworld", "the_nether", "the_end", "spawn", "arena"));
        for (var mark : ToolVisualsClient.GPS_POINTS.all()) if (!mark.target().world().isBlank()) worlds.add(mark.target().world());
        return List.copyOf(worlds);
    }

    private static List<String> coordinates() {
        var player = Minecraft.getInstance().player;
        return player == null ? List.of("0", "64", "0") : List.of(Long.toString((long)Math.floor(player.getX())),
                Long.toString((long)Math.floor(player.getY())), Long.toString((long)Math.floor(player.getZ())));
    }
}
