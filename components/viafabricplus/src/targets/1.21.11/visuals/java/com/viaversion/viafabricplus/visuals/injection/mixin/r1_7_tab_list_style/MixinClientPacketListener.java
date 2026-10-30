package com.viaversion.viafabricplus.visuals.injection.mixin.r1_7_tab_list_style;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;

import com.viaversion.viafabricplus.visuals.features.r1_7_tab_list_style.LegacyTabList;
import com.viaversion.viafabricplus.visuals.injection.access.r1_7_tab_list_tyle.IPlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void initPlayerListFix(ClientboundLoginPacket packet, CallbackInfo ci) {
        LegacyTabList.globalTablistIndex = 0;
        ((IPlayerTabOverlay) Minecraft.getInstance().gui.getTabList()).viaFabricPlusVisuals$setMaxPlayers(packet.maxPlayers());
    }

}
