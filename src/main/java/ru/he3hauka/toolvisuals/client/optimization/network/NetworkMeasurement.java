package ru.he3hauka.toolvisuals.client.optimization.network;

import io.netty.util.AttributeKey;

public final class NetworkMeasurement {
    public static final AttributeKey<NetworkCounter> COUNTER=AttributeKey.valueOf("toolvisuals:krypton_statistics");
    private NetworkMeasurement() {}
}
