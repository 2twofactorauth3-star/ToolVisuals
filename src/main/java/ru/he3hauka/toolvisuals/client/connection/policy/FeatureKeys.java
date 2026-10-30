package ru.he3hauka.toolvisuals.client.connection.policy;

import java.util.Map;

import net.minecraft.client.KeyMapping;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class FeatureKeys {
    private static final Map<String, VisualModule> MODULES = Map.ofEntries(
            Map.entry("key.toolvisuals.zoom", VisualModule.ZOOM),
            Map.entry("key.toolvisuals.free_look", VisualModule.FREE_LOOK),
            Map.entry("key.toolvisuals.change_hand", VisualModule.CHANGE_HAND),
            Map.entry("key.toolvisuals.elytra_swap", VisualModule.ELYTRA_SWAP),
            Map.entry("key.toolvisuals.fake_player", VisualModule.FAKE_PLAYER),
            Map.entry("key.toolvisuals.tape_mouse", VisualModule.TAPE_MOUSE),
            Map.entry("key.toolvisuals.emotes", VisualModule.ANIMATIONS),
            Map.entry("key.toolvisuals.paired_emotes", VisualModule.ANIMATIONS),
            Map.entry("key.toolvisuals.replay", VisualModule.REPLAY));

    private FeatureKeys() {}

    public static boolean allowed(ClientConfig config, String key) {
        var module = MODULES.get(key);
        return module == null || config.available(module);
    }

    public static KeyMapping[] filter(ClientConfig config, KeyMapping[] mappings) {
        return java.util.Arrays.stream(mappings).filter(key -> allowed(config, key.getName())).toArray(KeyMapping[]::new);
    }
}
