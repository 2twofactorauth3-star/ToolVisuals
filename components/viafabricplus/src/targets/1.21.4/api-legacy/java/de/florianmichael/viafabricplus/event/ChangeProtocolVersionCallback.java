package de.florianmichael.viafabricplus.event;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viafabricplus.LegacyCompatBridge;
import net.fabricmc.fabric.api.event.Event;

@Deprecated
public interface ChangeProtocolVersionCallback {

    @Deprecated
    Event<ChangeProtocolVersionCallback> EVENT = LegacyCompatBridge.createArrayBacked(ChangeProtocolVersionCallback.class, listeners -> (oldVersion, newVersion) -> {
        for (ChangeProtocolVersionCallback listener : listeners) {
            listener.onChangeProtocolVersion(oldVersion, newVersion);
        }
    });

    @Deprecated
    void onChangeProtocolVersion(final ProtocolVersion oldVersion, final ProtocolVersion newVersion);

}
