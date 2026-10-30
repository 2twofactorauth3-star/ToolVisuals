package de.florianmichael.viafabricplus.event;

import de.florianmichael.viafabricplus.LegacyCompatBridge;
import de.florianmichael.viafabricplus.settings.SettingsManager;
import net.fabricmc.fabric.api.event.Event;

@Deprecated
public interface RegisterSettingsCallback {

    @Deprecated
    Event<RegisterSettingsCallback> EVENT = LegacyCompatBridge.createArrayBacked(RegisterSettingsCallback.class, listeners -> (settingsManager, state) -> {
        for (RegisterSettingsCallback listener : listeners) {
            listener.onRegisterSettings(settingsManager, state);
        }
    });

    @Deprecated
    void onRegisterSettings(final SettingsManager settingsManager, final State state);

    @Deprecated
    enum State {
        PRE, POST
    }

}
