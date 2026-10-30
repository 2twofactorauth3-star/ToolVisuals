package ru.he3hauka.toolvisuals.client.replay.integration;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope;

public final class ReplayLifecycle {
    private ReplayLifecycle() {}
    public static void unload(ClientEntityEvents.Unload listener) {
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> { if (!ReplayScope.active()) listener.onUnload(entity, level); });
    }
}
