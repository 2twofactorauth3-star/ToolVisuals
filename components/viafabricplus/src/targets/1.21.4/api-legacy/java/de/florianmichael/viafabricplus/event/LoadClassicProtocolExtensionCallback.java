package de.florianmichael.viafabricplus.event;

import de.florianmichael.viafabricplus.LegacyCompatBridge;
import net.fabricmc.fabric.api.event.Event;
import net.raphimc.vialegacy.protocol.classic.c0_30cpetoc0_28_30.data.ClassicProtocolExtension;

@Deprecated
public interface LoadClassicProtocolExtensionCallback {

    @Deprecated
    Event<LoadClassicProtocolExtensionCallback> EVENT = LegacyCompatBridge.createArrayBacked(LoadClassicProtocolExtensionCallback.class, listeners -> classicProtocolExtension -> {
        for (LoadClassicProtocolExtensionCallback listener : listeners) {
            listener.onLoadClassicProtocolExtension(classicProtocolExtension);
        }
    });

    @Deprecated
    void onLoadClassicProtocolExtension(final ClassicProtocolExtension classicProtocolExtension);

}
