package de.florianmichael.viafabricplus.fixes.data;

import net.minecraft.item.Item;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viafabricplus.LegacyCompatBridge;

@Deprecated
public class ItemRegistryDiff {

    @Deprecated
    public static boolean keepItem(final Item item) {
        LegacyCompatBridge.warn();
        return ViaFabricPlus.getImpl().itemExistsInConnection(item);
    }

    @Deprecated
    public static boolean contains(final Item item, final ProtocolVersion version) {
        LegacyCompatBridge.warn();
        return ViaFabricPlus.getImpl().itemExists(item, version);
    }

}
