package ru.he3hauka.toolvisuals.client.render.particles;

import java.util.Locale;
import java.util.Set;
import net.minecraft.client.particle.ExplodeParticle;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.HugeExplosionSeedParticle;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

public final class ParticleVisibility {
    private static final Set<String> EXPLOSION_CLASSES = Set.of(ExplodeParticle.class.getName(),
            HugeExplosionParticle.class.getName(), HugeExplosionSeedParticle.class.getName());

    private ParticleVisibility() {
    }

    public static boolean hides(ClientConfig config, String name) {
        if (isVanillaExplosion(name) && config.active(VisualModule.OPTIMIZATION)
                && config.flag(VisualSetting.OPT_HIDE_EXPLOSIONS) && config.available(RenderOption.EXPLOSION_PARTICLES)) return true;
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

    private static boolean isVanillaExplosion(String name) {
        return EXPLOSION_CLASSES.contains(name) || name.equals("minecraft:explosion") || name.equals("minecraft:explosion_emitter")
                || name.equals("explosion") || name.equals("explosion_emitter");
    }
}
