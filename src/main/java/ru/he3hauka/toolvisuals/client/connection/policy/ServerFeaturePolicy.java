package ru.he3hauka.toolvisuals.client.connection.policy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ServerFeaturePolicy {
    private volatile Set<String> blocked = Set.of();
    private volatile long revision;

    public boolean allows(String id) {
        return !blocked.contains(id);
    }

    public long revision() {
        return revision;
    }

    public Set<String> blocked() {
        return blocked;
    }

    public void update(boolean holyWorld, Collection<String> remote) {
        var next = new HashSet<String>();
        if (holyWorld) next.addAll(HolyWorldRules.BLOCKED);
        for (String id : remote) {
            String known = FeatureCatalog.resolve(id);
            if (known != null) next.add(known);
        }
        if (next.contains(ru.he3hauka.toolvisuals.client.module.VisualModule.NO_FLUID.id)) {
            next.add(FeatureCatalog.option(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.FOG));
            next.add(FeatureCatalog.option(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.WATER_OVERLAY));
        }
        if (next.contains(ru.he3hauka.toolvisuals.client.module.VisualModule.NO_HURT_CAM.id)) {
            for (var option : java.util.List.of(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.HURT_CAMERA,
                    ru.he3hauka.toolvisuals.client.render.filter.RenderOption.VIEW_BOBBING,
                    ru.he3hauka.toolvisuals.client.render.filter.RenderOption.NAUSEA,
                    ru.he3hauka.toolvisuals.client.render.filter.RenderOption.PORTAL_DISTORTION)) next.add(FeatureCatalog.option(option));
        }
        var snapshot = Set.copyOf(next);
        if (snapshot.equals(blocked)) return;
        blocked = snapshot;
        revision++;
    }

    public void clear() {
        update(false, Set.of());
    }
}
