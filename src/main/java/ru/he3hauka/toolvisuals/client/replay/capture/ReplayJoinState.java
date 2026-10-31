package ru.he3hauka.toolvisuals.client.replay.capture;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;

public final class ReplayJoinState {
    public final ClientPacketListener listener;
    public final ClientboundLoginPacket login;
    public ClientboundRespawnPacket respawn;

    public ReplayJoinState(ClientPacketListener listener, ClientboundLoginPacket login) {
        this.listener = listener;
        this.login = login;
    }
}
