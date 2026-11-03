package ru.he3hauka.toolvisuals.client.render.particles;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.client.particle.*;
import net.minecraft.client.particle.ExplodeParticle;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.HugeExplosionSeedParticle;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

public final class ParticleVisibility {
    private static final Map<String, RenderOption> AUTOMATIC = automaticTypes();

    private static Map<String, RenderOption> automaticTypes() {
        var types = new HashMap<String, RenderOption>();
        add(types, RenderOption.EXPLOSION_PARTICLES, new String[] {"explosion", "explosion_emitter"},
                ExplodeParticle.class, HugeExplosionParticle.class, HugeExplosionSeedParticle.class);
        add(types, RenderOption.SMOKE_PARTICLES, new String[] {"smoke", "large_smoke", "white_smoke", "campfire_cosy_smoke", "campfire_signal_smoke"},
                SmokeParticle.class, LargeSmokeParticle.class, WhiteSmokeParticle.class, CampfireSmokeParticle.class);
        add(types, RenderOption.PORTAL_PARTICLES, new String[] {"portal", "reverse_portal"}, PortalParticle.class, ReversePortalParticle.class);
        return Map.copyOf(types);
    }

    private static void add(Map<String, RenderOption> target, RenderOption option, String[] ids, Class<?>... classes) {
        for (String id : ids) { target.put(id, option); target.put("minecraft:" + id, option); }
        for (Class<?> type : classes) target.put(type.getName(), option);
    }

    public static boolean hidesOptimized(ClientConfig config, RenderOption option) {
        var setting = switch (option) {
            case EXPLOSION_PARTICLES -> VisualSetting.OPT_HIDE_EXPLOSIONS;
            case SMOKE_PARTICLES -> VisualSetting.OPT_HIDE_SMOKE;
            case PORTAL_PARTICLES -> VisualSetting.OPT_HIDE_PORTAL_PARTICLES;
            default -> null;
        };
        return setting != null && config.active(VisualModule.OPTIMIZATION) && config.flag(setting) && config.available(option);
    }

    private ParticleVisibility() {
    }

    public static boolean hides(ClientConfig config, String name) {
        var automatic = AUTOMATIC.get(name);
        if (automatic != null && hidesOptimized(config, automatic)) return true;
        if (!config.enabled()) return false;
        if (config.hides(RenderOption.ALL_PARTICLES)) return true;
        if (!config.hides(RenderOption.SMOKE_PARTICLES) && !config.hides(RenderOption.EXPLOSION_PARTICLES)
                && !config.hides(RenderOption.FIREWORK_PARTICLES) && !config.hides(RenderOption.POTION_PARTICLES)
                && !config.hides(RenderOption.PORTAL_PARTICLES) && !config.hides(RenderOption.TOTEM_PARTICLES)
                && !config.hides(RenderOption.BLOCK_PARTICLES)) return false;
        String id = name.toLowerCase(Locale.ROOT);
        int namespace = id.indexOf(':');
        if (namespace >= 0) id = id.substring(namespace + 1);
        return (id.contains("smoke") || id.contains("ash")) && config.hides(RenderOption.SMOKE_PARTICLES)
                || (id.contains("explosion") || id.contains("explode") || id.contains("gust")) && config.hides(RenderOption.EXPLOSION_PARTICLES)
                || (id.contains("firework") || id.contains("flash")) && config.hides(RenderOption.FIREWORK_PARTICLES)
                || (id.contains("effect") || id.contains("spell") || id.contains("dragonbreath") || id.contains("dragon_breath")) && config.hides(RenderOption.POTION_PARTICLES)
                || id.contains("portal") && config.hides(RenderOption.PORTAL_PARTICLES)
                || id.contains("totem") && config.hides(RenderOption.TOTEM_PARTICLES)
                || (id.contains("terrain") || id.contains("block") || id.contains("fallingdust") || id.contains("falling_dust")) && config.hides(RenderOption.BLOCK_PARTICLES);
    }

}
