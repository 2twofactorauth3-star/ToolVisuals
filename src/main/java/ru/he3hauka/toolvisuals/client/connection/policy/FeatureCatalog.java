package ru.he3hauka.toolvisuals.client.connection.policy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

public final class FeatureCatalog {
    private static final Map<String, String> ALIASES = aliases();
    public static final List<String> IDS = ALIASES.values().stream().distinct().sorted().toList();

    private FeatureCatalog() {}

    public static String option(RenderOption option) {
        return option.featureId;
    }

    public static String resolve(String id) {
        return id == null ? null : ALIASES.get(normalize(id));
    }

    private static Map<String, String> aliases() {
        var aliases = new LinkedHashMap<String, String>();
        for (var module : VisualModule.values()) {
            add(aliases, module.id, module.id);
            add(aliases, module.title, module.id);
        }
        for (var feature : ClientFeature.values()) add(aliases, feature.id, feature.id);
        for (var option : RenderOption.values()) add(aliases, option(option), option(option));
        return Map.copyOf(aliases);
    }

    private static void add(Map<String, String> aliases, String alias, String id) {
        String previous = aliases.putIfAbsent(normalize(alias), id);
        if (previous != null && !previous.equals(id)) throw new IllegalStateException("Duplicate feature ID: " + alias);
    }

    private static String normalize(String id) {
        return id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
