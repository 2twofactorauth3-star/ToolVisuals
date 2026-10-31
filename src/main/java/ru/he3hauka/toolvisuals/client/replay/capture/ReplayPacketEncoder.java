package ru.he3hauka.toolvisuals.client.replay.capture;

import io.netty.buffer.Unpooled;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import ru.he3hauka.toolvisuals.client.replay.io.ReplayFormat;
import ru.he3hauka.toolvisuals.client.replay.io.ReplayFrame;

public final class ReplayPacketEncoder {
    private ReplayPacketEncoder() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ReplayFrame encode(ProtocolInfo<?> protocol, Packet<?> packet, long millis) {
        var buffer = Unpooled.buffer(256, ReplayFormat.MAX_PACKET_BYTES);
        try {
            ((net.minecraft.network.codec.StreamCodec) protocol.codec()).encode(buffer, packet);
            var bytes = new byte[buffer.readableBytes()];
            buffer.getBytes(0, bytes);
            return ReplayFrame.takeOwnership(protocol.id() == ConnectionProtocol.CONFIGURATION
                    ? ReplayFrame.Kind.CONFIGURATION : ReplayFrame.Kind.PLAY, millis, bytes);
        } finally {
            buffer.release();
        }
    }
}
