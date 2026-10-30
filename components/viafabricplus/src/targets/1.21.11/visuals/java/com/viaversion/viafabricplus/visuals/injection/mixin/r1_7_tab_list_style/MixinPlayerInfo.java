package com.viaversion.viafabricplus.visuals.injection.mixin.r1_7_tab_list_style;

import net.minecraft.client.multiplayer.PlayerInfo;

import com.viaversion.viafabricplus.visuals.features.r1_7_tab_list_style.LegacyTabList;
import com.viaversion.viafabricplus.visuals.injection.access.r1_7_tab_list_tyle.IPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerInfo.class)
public abstract class MixinPlayerInfo implements IPlayerInfo {

    @Unique
    private final int viaFabricPlusVisuals$index = LegacyTabList.globalTablistIndex++;

    @Override
    public int viaFabricPlusVisuals$getIndex() {
        return viaFabricPlusVisuals$index;
    }
}
