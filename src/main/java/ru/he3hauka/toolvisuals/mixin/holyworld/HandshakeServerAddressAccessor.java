package ru.he3hauka.toolvisuals.mixin.holyworld;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientHandshakePacketListenerImpl.class)
public interface HandshakeServerAddressAccessor {
    @Accessor("serverData") ServerData toolvisuals$serverData();
}
