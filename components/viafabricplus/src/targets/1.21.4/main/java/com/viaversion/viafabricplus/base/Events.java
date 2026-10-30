package com.viaversion.viafabricplus.base;

import com.viaversion.viafabricplus.api.events.ChangeProtocolVersionCallback;
import com.viaversion.viafabricplus.api.events.LoadingCycleCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class Events {

    public static final Event<LoadingCycleCallback> LOADING_CYCLE = EventFactory.createArrayBacked(LoadingCycleCallback.class, listeners -> state -> {
        for (LoadingCycleCallback listener : listeners) {
            listener.onLoadCycle(state);
        }
    });

    public static final Event<ChangeProtocolVersionCallback> CHANGE_PROTOCOL_VERSION = EventFactory.createArrayBacked(ChangeProtocolVersionCallback.class, listeners -> (oldVersion, newVersion) -> {
        for (ChangeProtocolVersionCallback listener : listeners) {
            listener.onChangeProtocolVersion(oldVersion, newVersion);
        }
    });

}
