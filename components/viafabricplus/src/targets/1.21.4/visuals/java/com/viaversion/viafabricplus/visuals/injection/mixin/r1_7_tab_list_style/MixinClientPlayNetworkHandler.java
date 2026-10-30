package com.viaversion.viafabricplus.visuals.injection.mixin.r1_7_tab_list_style;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

import com.viaversion.viafabricplus.visuals.features.r1_7_tab_list_style.LegacyTabList;
import com.viaversion.viafabricplus.visuals.injection.access.r1_7_tab_list_tyle.IPlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void initPlayerListFix(GameJoinS2CPacket packet, CallbackInfo ci) {
        LegacyTabList.globalTablistIndex = 0;
        ((IPlayerListHud) MinecraftClient.getInstance().inGameHud.getPlayerListHud()).viaFabricPlusVisuals$setMaxPlayers(packet.maxPlayers());
    }

}
