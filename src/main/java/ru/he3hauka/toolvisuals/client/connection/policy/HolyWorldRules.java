package ru.he3hauka.toolvisuals.client.connection.policy;

import java.util.Map;
import java.util.Set;

import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class HolyWorldRules {
    public enum Basis { RULES, PRECAUTION }

    public static final Map<VisualModule, Basis> MODULES = Map.ofEntries(
            Map.entry(VisualModule.ELYTRA_SWAP, Basis.RULES),
            Map.entry(VisualModule.PROJECTILE_PREDICTION, Basis.RULES),
            Map.entry(VisualModule.CRYSTAL_OPTIMIZER, Basis.RULES),
            Map.entry(VisualModule.TAPE_MOUSE, Basis.RULES),
            Map.entry(VisualModule.AUCTION_HELPER, Basis.RULES),
            Map.entry(VisualModule.REPLAY, Basis.RULES),
            Map.entry(VisualModule.FAST_EXP, Basis.PRECAUTION),
            Map.entry(VisualModule.TARGET_ESP, Basis.PRECAUTION),
            Map.entry(VisualModule.CHUNK_ANIMATOR, Basis.PRECAUTION),
            Map.entry(VisualModule.NO_FLUID, Basis.PRECAUTION),
            Map.entry(VisualModule.RESOURCE_PACK_SPOOFER, Basis.PRECAUTION),
            Map.entry(VisualModule.KEYBINDS, Basis.PRECAUTION),
            Map.entry(VisualModule.TOOL_PRESENTS, Basis.PRECAUTION));
    public static final Set<String> BLOCKED = MODULES.keySet().stream().map(module -> module.id)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    private HolyWorldRules() {}
}
