package ru.he3hauka.toolvisuals.mixin.holyworld;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientCommonPacketListenerImpl.class)
public interface ServerAddressAccessor {
    @Accessor("serverData") ServerData toolvisuals$serverData();
}
