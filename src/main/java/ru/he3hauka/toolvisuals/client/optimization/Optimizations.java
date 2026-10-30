package ru.he3hauka.toolvisuals.client.optimization;

import java.util.EnumSet;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.optimization.text.PreparedTextCache;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class Optimizations {
    private static final VisualSetting[] FEATURES = {OPT_PREPARED_TEXT,OPT_UI_METRICS,OPT_PARTICLE_LIGHT,
            OPT_FAST_VISUALS,OPT_FAST_HUD,OPT_ENTITY_CULLING,OPT_BLOCK_ENTITY_CULLING,OPT_EFFECT_BUDGET,
            OPT_HIDDEN_FACES,OPT_STATIC_BLOCK_ENTITIES,OPT_CHUNK_BUDGET};
    private static volatile EnumSet<VisualSetting> enabled = EnumSet.copyOf(java.util.List.of(FEATURES));
    private Optimizations() {}
    public static boolean enabled(VisualSetting feature) { return enabled.contains(feature); }
    public static void update(ClientConfig config) {
        var next = EnumSet.noneOf(VisualSetting.class);
        if (config.active(VisualModule.OPTIMIZATION))
            for (var feature : FEATURES)
                if (config.flag(feature)) next.add(feature);
        if (enabled.equals(next)) return;
        enabled = next; PreparedTextCache.invalidate();
    }
}
