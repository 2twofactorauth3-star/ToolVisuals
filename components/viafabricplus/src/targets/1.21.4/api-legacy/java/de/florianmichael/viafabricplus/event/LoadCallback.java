package de.florianmichael.viafabricplus.event;

import de.florianmichael.viafabricplus.LegacyCompatBridge;
import net.fabricmc.fabric.api.event.Event;

@Deprecated
public interface LoadCallback {

    @Deprecated
    Event<LoadCallback> EVENT = LegacyCompatBridge.createArrayBacked(LoadCallback.class, listeners -> state -> {
        for (LoadCallback listener : listeners) {
            listener.onLoad(state);
        }
    });

    @Deprecated
    void onLoad(final State state);

    @Deprecated
    enum State {
        PRE, POST
    }

}
