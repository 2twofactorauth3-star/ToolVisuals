package com.viaversion.viafabricplus.visuals.injection.mixin.r1_7_tab_list_style;

import net.minecraft.client.network.PlayerListEntry;

import com.viaversion.viafabricplus.visuals.features.r1_7_tab_list_style.LegacyTabList;
import com.viaversion.viafabricplus.visuals.injection.access.r1_7_tab_list_tyle.IPlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerListEntry implements IPlayerListEntry {

    @Unique
    private final int viaFabricPlusVisuals$index = LegacyTabList.globalTablistIndex++;

    @Override
    public int viaFabricPlusVisuals$getIndex() {
        return viaFabricPlusVisuals$index;
    }
}
