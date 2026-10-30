package ru.he3hauka.toolvisuals.client.replay.capture;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.replay.io.*;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.REPLAY;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ReplayCapture {
    private static final class Session {
        final Connection connection;
        final ReplayWriter writer;
        final long started = System.nanoTime();
        final ReplaySelf self = new ReplaySelf();
        Session(Connection connection, ReplayWriter writer) { this.connection = connection; this.writer = writer; }
        long millis() { return Math.clamp((System.nanoTime() - started) / 1000000, 0, ReplayFormat.MAX_DURATION_MILLIS); }
    }

    private static volatile Session session;
    private static volatile CompletableFuture<ReplaySnapshot> last;
    private static volatile Path directory;

    private ReplayCapture() {}

    public static void initialize(Minecraft client) {
        directory = client.gameDirectory.toPath().resolve("toolvisuals/replays").toAbsolutePath().normalize();
    }

    public static Path directory() { return directory; }
    public static boolean recording() { return session != null; }
    public static long duration() { var current = session; return current == null ? 0 : current.millis(); }

    public static synchronized void protocol(Connection connection, ProtocolInfo<?> protocol) {
        if (connection instanceof VirtualReplayConnection || connection.getReceiving() != PacketFlow.CLIENTBOUND
                || protocol.id() != ConnectionProtocol.CONFIGURATION || directory == null || !ToolVisualsClient.CONFIG.active(REPLAY)) return;
        if (session != null && session.connection == connection) return;
        stop();
        try {
            var client = Minecraft.getInstance(); var user = client.getUser(); var version = SharedConstants.getCurrentVersion();
            var header = new ReplayHeader(UUID.randomUUID(), user.getProfileId(), user.getName(), version.name(), version.protocolVersion(), System.currentTimeMillis());
            var file = directory.resolve(header.createdMillis + "-" + header.recording + ".tvr");
            session = new Session(connection, new ReplayWriter(file, header, (long)ToolVisualsClient.CONFIG.value(REPLAY_FILE_LIMIT) * 1024 * 1024));
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot start replay recording", error);
            Minecraft.getInstance().execute(() -> ChatMessages.notice("replay.record_error"));
        }
    }

    private static Session current(Connection connection, ProtocolInfo<?> protocol, Packet<?> packet) {
        var current = session;
        if (current == null || current.connection != connection || !ToolVisualsClient.CONFIG.active(REPLAY)
                || protocol == null || protocol.id() != ConnectionProtocol.CONFIGURATION && protocol.id() != ConnectionProtocol.PLAY) return null;
        var id = packet.type().id();
        return ReplayPacketPolicy.allow(protocol.id() == ConnectionProtocol.CONFIGURATION, id.getNamespace(), id.getPath(),
                ToolVisualsClient.CONFIG.flag(REPLAY_CHAT)) ? current : null;
    }

    public static void raw(Connection connection, ProtocolInfo<?> protocol, Packet<?> packet, ByteBuf buffer, int start, int end) {
        var current = current(connection, protocol, packet);
        if (current == null) return;
        int size = end - start;
        if (size < 1 || size > ReplayFormat.MAX_PACKET_BYTES) { fail(current, new IllegalArgumentException("Replay packet exceeds the file format limit")); return; }
        var bytes = new byte[size]; buffer.getBytes(start, bytes);
        current.writer.offer(ReplayFrame.takeOwnership(protocol.id() == ConnectionProtocol.CONFIGURATION
                ? ReplayFrame.Kind.CONFIGURATION : ReplayFrame.Kind.PLAY, current.millis(), bytes));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void memory(Connection connection, ProtocolInfo<?> protocol, Packet<?> packet) {
        if (packet instanceof BundlePacket<?> bundle) { for (var part : bundle.subPackets()) memory(connection, protocol, part); return; }
        var current = current(connection, protocol, packet);
        if (current == null) return;
        ByteBuf buffer = Unpooled.buffer(256, ReplayFormat.MAX_PACKET_BYTES);
        try {
            ((net.minecraft.network.codec.StreamCodec)protocol.codec()).encode(buffer, packet);
            raw(connection, protocol, packet, buffer, 0, buffer.writerIndex());
        } catch (RuntimeException error) { fail(current, error); }
        finally { buffer.release(); }
    }

    public static CompletableFuture<ReplaySnapshot> snapshot() {
        var current = session;
        if (current != null) return current.writer.snapshot();
        return last == null ? CompletableFuture.failedFuture(new java.io.IOException("No replay has been recorded")) : last;
    }

    public static synchronized void stop() {
        var current = session; session = null;
        if (current != null) last = current.writer.finish();
    }

    public static void shutdown() {
        stop(); var pending = last;
        if (pending != null) try { pending.get(3, java.util.concurrent.TimeUnit.SECONDS); }
        catch (Exception error) { org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Replay finalization did not finish before shutdown", error); }
    }

    private static synchronized void fail(Session expected, Throwable error) {
        if (session != expected) return;
        stop();
        org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Replay recording stopped; the existing recording was preserved", error);
        Minecraft.getInstance().execute(() -> ChatMessages.notice("replay.record_error"));
    }

    public static void tick(Minecraft client) {
        var current = session;
        if (current == null) return;
        if (!ToolVisualsClient.CONFIG.active(REPLAY) || !current.connection.isConnected()) { stop(); return; }
        if (current.writer.failure() != null) { fail(current, current.writer.failure()); return; }
        if (client.player != null && client.player.connection.getConnection() == current.connection) {
            try { current.writer.offer(current.self.capture(client.player, current.millis())); }
            catch (RuntimeException error) { fail(current, error); return; }
        }
        if (current.millis() >= ReplayFormat.MAX_DURATION_MILLIS) stop();
    }
}
