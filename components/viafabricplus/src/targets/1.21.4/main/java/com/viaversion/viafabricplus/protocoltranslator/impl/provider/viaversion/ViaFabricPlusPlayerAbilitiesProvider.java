package com.viaversion.viafabricplus.protocoltranslator.impl.provider.viaversion;

import net.minecraft.client.MinecraftClient;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.provider.PlayerAbilitiesProvider;

public final class ViaFabricPlusPlayerAbilitiesProvider extends PlayerAbilitiesProvider {

    @Override
    public float getFlyingSpeed(UserConnection connection) {
        return MinecraftClient.getInstance().player.getAbilities().getFlySpeed();
    }

    @Override
    public float getWalkingSpeed(UserConnection connection) {
        return MinecraftClient.getInstance().player.getAbilities().getWalkSpeed();
    }

}
