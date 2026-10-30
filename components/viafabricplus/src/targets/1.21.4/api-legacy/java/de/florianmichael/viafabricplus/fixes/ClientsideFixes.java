package de.florianmichael.viafabricplus.fixes;

import com.viaversion.viafabricplus.ViaFabricPlus;
import de.florianmichael.viafabricplus.LegacyCompatBridge;

@Deprecated
public class ClientsideFixes {

    @Deprecated
    public static int getChatLength() {
        LegacyCompatBridge.warn();
        return ViaFabricPlus.getImpl().getMaxChatLength(ViaFabricPlus.getImpl().getTargetVersion());
    }

}
