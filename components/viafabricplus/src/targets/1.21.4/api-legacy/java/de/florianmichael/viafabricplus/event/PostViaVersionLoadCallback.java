package de.florianmichael.viafabricplus.event;

import de.florianmichael.viafabricplus.LegacyCompatBridge;
import net.fabricmc.fabric.api.event.Event;

@Deprecated
public interface PostViaVersionLoadCallback {

    @Deprecated
    Event<PostViaVersionLoadCallback> EVENT = LegacyCompatBridge.createArrayBacked(PostViaVersionLoadCallback.class, listeners -> () -> {
        for (PostViaVersionLoadCallback listener : listeners) {
            listener.onPostViaVersionLoad();
        }
    });

    @Deprecated
    void onPostViaVersionLoad();

}
