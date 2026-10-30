package ru.he3hauka.toolvisuals.client.presents.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire;

public interface WorldContextMessage extends CustomPacketPayload {
    WorldContextWire.State value();
}
