package ru.he3hauka.toolvisuals.minigame.network;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

import ru.he3hauka.toolvisuals.minigame.GameKind;

public final class MiniGameWire {
    public static final String REQUEST_CHANNEL = "toolvisuals:minigames_c2s_v1", STATE_CHANNEL = "toolvisuals:minigames_s2c_v1";
    public static final int MAX_REQUEST = 54, MAX_STATE = 256;
    public static final UUID NONE = new UUID(0, 0);
    public enum Action {
        HELLO(0), INVITE(1), ANSWER(2), MOVE(3), LEAVE(4), POLL(5);
        public final int id;
        Action(int id) { this.id = id; }
        static Action byId(int id) { for (var value : values()) if (value.id == id) return value; throw new IllegalArgumentException("Unknown game action"); }
    }
    public enum Code {
        READY(0), OFF(1), WAITING(2), INVITED(3), MATCH(4), CLOSED(5), DECLINED(6), EXPIRED(7), UNAVAILABLE(8), BUSY(9), STALE(10), INVALID(11), CAPACITY(12);
        public final int id;
        Code(int id) { this.id = id; }
        static Code byId(int id) { for (var value : values()) if (value.id == id) return value; throw new IllegalArgumentException("Unknown game result"); }
    }
    public record Command(UUID session, long id, Action action, UUID reference, GameKind kind, int revision, int first, int second) {
        public Command {
            Objects.requireNonNull(session); Objects.requireNonNull(reference); Objects.requireNonNull(action); Objects.requireNonNull(kind);
            if (session.equals(NONE) || id <= 0 || revision < 0 || revision > 42 || first < 0 || first > 100 || second < 0 || second > 100)
                throw new IllegalArgumentException("Invalid game command");
            if (action != Action.MOVE && (revision != 0 || second != 0 || first > (action == Action.HELLO || action == Action.ANSWER ? 1 : 0)))
                throw new IllegalArgumentException("Unexpected game command fields");
            if ((action == Action.INVITE || action == Action.ANSWER || action == Action.MOVE) && reference.equals(NONE))
                throw new IllegalArgumentException("Missing game reference");
        }
    }
    public record Message(UUID session, long reply, Code code, UUID room, UUID first, UUID second, GameKind kind, long seed, long invitationRequest, int[] history, int seconds) {
        public Message {
            Objects.requireNonNull(session); Objects.requireNonNull(code); Objects.requireNonNull(room); Objects.requireNonNull(first); Objects.requireNonNull(second); Objects.requireNonNull(kind);
            if (session.equals(NONE) || reply < 0 || invitationRequest < 0 || seconds < 0 || seconds > 30 || history.length % 2 != 0 || history.length > kind.maximumMoves * 2)
                throw new IllegalArgumentException("Invalid game message");
            for (int value : history) if (value < 0 || value > 100) throw new IllegalArgumentException("Invalid game history value");
            if ((code == Code.MATCH || code == Code.WAITING || code == Code.INVITED)
                    && (room.equals(NONE) || first.equals(NONE) || second.equals(NONE) || first.equals(second))) throw new IllegalArgumentException("Invalid game participants");
            if (code != Code.MATCH && history.length != 0) throw new IllegalArgumentException("Unexpected game history");
            history = history.clone();
        }
        @Override public int[] history() { return history.clone(); }
    }

    private MiniGameWire() {}
    private static UUID uuid(DataInputStream in) throws IOException { return new UUID(in.readLong(), in.readLong()); }
    private static void uuid(DataOutputStream out, UUID id) throws IOException { out.writeLong(id.getMostSignificantBits()); out.writeLong(id.getLeastSignificantBits()); }

    public static byte[] encode(Command value) {
        try {
            var bytes = new ByteArrayOutputStream(MAX_REQUEST); var out = new DataOutputStream(bytes);
            uuid(out, value.session()); out.writeLong(value.id()); out.writeByte(value.action().id); uuid(out, value.reference());
            out.writeByte(value.kind().id); out.writeInt(value.revision()); out.writeInt(value.first()); out.writeInt(value.second());
            return bytes.toByteArray();
        } catch (IOException impossible) { throw new AssertionError(impossible); }
    }

    public static Command command(byte[] bytes) {
        if (bytes == null || bytes.length != MAX_REQUEST) throw new IllegalArgumentException("Invalid game command length");
        try {
            var in = new DataInputStream(new ByteArrayInputStream(bytes));
            return new Command(uuid(in), in.readLong(), Action.byId(in.readUnsignedByte()), uuid(in), GameKind.byId(in.readUnsignedByte()), in.readInt(), in.readInt(), in.readInt());
        } catch (IOException error) { throw new IllegalArgumentException("Truncated game command", error); }
    }

    public static byte[] encode(Message value) {
        try {
            var bytes = new ByteArrayOutputStream(MAX_STATE); var out = new DataOutputStream(bytes);
            uuid(out, value.session()); out.writeLong(value.reply()); out.writeByte(value.code().id); uuid(out, value.room()); uuid(out, value.first()); uuid(out, value.second());
            out.writeByte(value.kind().id); out.writeLong(value.seed()); out.writeLong(value.invitationRequest()); out.writeByte(value.seconds()); out.writeByte(value.history.length);
            for (int move : value.history) out.writeByte(move);
            return bytes.toByteArray();
        } catch (IOException impossible) { throw new AssertionError(impossible); }
    }

    public static Message message(byte[] bytes) {
        if (bytes == null || bytes.length < 92 || bytes.length > MAX_STATE) throw new IllegalArgumentException("Invalid game message length");
        try {
            var in = new DataInputStream(new ByteArrayInputStream(bytes));
            UUID session = uuid(in); long reply = in.readLong(); Code code = Code.byId(in.readUnsignedByte());
            UUID room = uuid(in), first = uuid(in), second = uuid(in); GameKind kind = GameKind.byId(in.readUnsignedByte());
            long seed = in.readLong(), origin = in.readLong(); int seconds = in.readUnsignedByte(), count = in.readUnsignedByte();
            if (count > kind.maximumMoves * 2 || count != in.available()) throw new IllegalArgumentException("Invalid game history length");
            int[] history = new int[count]; for (int i = 0; i < count; i++) history[i] = in.readUnsignedByte();
            return new Message(session, reply, code, room, first, second, kind, seed, origin, history, seconds);
        } catch (IOException error) { throw new IllegalArgumentException("Truncated game message", error); }
    }
}
