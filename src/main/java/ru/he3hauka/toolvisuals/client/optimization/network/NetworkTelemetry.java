package ru.he3hauka.toolvisuals.client.optimization.network;

import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.mixin.optimization.network.ConnectionChannelAccess;

public final class NetworkTelemetry {
    private static Channel channel;
    private static NetworkCounter counter;
    private NetworkTelemetry() {}

    public static void tick(Minecraft client){
        var connection=client.getConnection();
        Channel next=NetworkOptimizations.enabled()&&ToolVisualsClient.CONFIG.flag(VisualSetting.KRYPTON_STATISTICS)&&connection!=null
                ?((ConnectionChannelAccess)(Object)connection.getConnection()).toolvisuals$channel():null;
        if(next!=null&&(!next.isActive()||connection.getConnection().isMemoryConnection()))next=null;
        if(next==channel)return;
        if(channel!=null)channel.attr(NetworkMeasurement.COUNTER).set(null);
        channel=next;counter=next==null?null:new NetworkCounter();
        if(next!=null)next.attr(NetworkMeasurement.COUNTER).set(counter);
    }
    public static NetworkCounter.Snapshot snapshot(){return counter==null?NetworkCounter.EMPTY:counter.snapshot();}
    public static boolean measuring(){return counter!=null;}
}
