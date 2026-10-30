package ru.he3hauka.toolvisuals.client.optimization.occlusion;

import dev.tr7zw.entityculling.EntityCullingModBase;
import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.hud.target.VisualTarget;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class EntityCullingBridge {
    private static EntityCullingModBase installed;
    private EntityCullingBridge() {}
    public static void tick(Minecraft client) {
        var mod = EntityCullingModBase.instance;
        if (mod == null || mod.config == null) return;
         
        mod.config.tickCulling = false;
        boolean replay=client.gui.screen() instanceof ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen;
        mod.config.skipEntityCulling = replay||!Optimizations.enabled(OPT_ENTITY_CULLING);
        mod.config.skipBlockEntityCulling = replay||!Optimizations.enabled(OPT_BLOCK_ENTITY_CULLING);
         
        if (VisualTarget.current() instanceof Cullable target) target.setTimeout();
        if (installed != mod) {
            installed = mod;
            mod.addDynamicEntityWhitelist(entity -> entity == client.player || entity == VisualTarget.current()
                    || client.shouldEntityAppearGlowing(entity));
        }
    }
}
