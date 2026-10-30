package de.florianmichael.viafabricplus.event;

import de.florianmichael.viafabricplus.LegacyCompatBridge;
import net.fabricmc.fabric.api.event.Event;

@Deprecated
public interface PostGameLoadCallback {

    @Deprecated
    Event<PostGameLoadCallback> EVENT = LegacyCompatBridge.createArrayBacked(PostGameLoadCallback.class, listeners -> () -> {
        for (PostGameLoadCallback listener : listeners) {
            listener.postGameLoad();
        }
    });

    @Deprecated
    void postGameLoad();

}
