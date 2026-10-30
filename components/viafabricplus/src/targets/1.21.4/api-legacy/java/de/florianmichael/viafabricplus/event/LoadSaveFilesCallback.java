package de.florianmichael.viafabricplus.event;

import de.florianmichael.viafabricplus.LegacyCompatBridge;
import de.florianmichael.viafabricplus.save.SaveManager;
import net.fabricmc.fabric.api.event.Event;

@Deprecated
public interface LoadSaveFilesCallback {

    @Deprecated
    Event<LoadSaveFilesCallback> EVENT = LegacyCompatBridge.createArrayBacked(LoadSaveFilesCallback.class, listeners -> (saveManager, state) -> {
        for (LoadSaveFilesCallback listener : listeners) {
            listener.onLoadSaveFiles(saveManager, state);
        }
    });

    @Deprecated
    void onLoadSaveFiles(final SaveManager saveManager, final State state);

    @Deprecated
    enum State {
        PRE, POST, POST_INIT
    }

}
