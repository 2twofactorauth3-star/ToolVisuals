package ru.he3hauka.toolvisuals.client.replay.playback;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import ru.he3hauka.toolvisuals.client.replay.capture.VirtualReplayConnection;

final class ReplayConnection extends Connection implements VirtualReplayConnection, AutoCloseable {
    private final EmbeddedChannel embedded;
    private ProtocolInfo<?> inbound;
    private boolean closed;

    ReplayConnection() {
        super(PacketFlow.CLIENTBOUND);
        embedded = new EmbeddedChannel();
        Connection.configureInMemoryPipeline(embedded.pipeline(), PacketFlow.CLIENTBOUND);
        embedded.pipeline().addLast("packet_handler", this);
        embedded.pipeline().fireChannelActive();
    }

    ProtocolInfo<?> inbound() { return inbound; }

    @Override public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> protocol, T listener) {
        inbound = protocol; super.setupInboundProtocol(protocol, listener);
    }

    @Override public void send(Packet<?> packet) {}
    @Override public void send(Packet<?> packet, ChannelFutureListener listener) { send(packet, listener, true); }
    @Override public void send(Packet<?> packet, ChannelFutureListener listener, boolean flush) {
        if (listener != null) try { listener.operationComplete(embedded.newSucceededFuture()); }
        catch (Exception error) { throw new IllegalStateException("Cannot complete a virtual replay packet", error); }
    }
    @Override public boolean isConnected() { return !closed; }
    @Override public boolean isMemoryConnection() { return false; }
    @Override public void disconnect(Component reason) { closed = true; }
    @Override public void disconnect(DisconnectionDetails reason) { closed = true; }
    @Override public void handleDisconnection() {}
    @Override public void close() { closed = true; embedded.finishAndReleaseAll(); }
}
