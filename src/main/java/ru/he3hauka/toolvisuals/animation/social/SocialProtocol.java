package ru.he3hauka.toolvisuals.animation.social;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import ru.he3hauka.toolvisuals.animation.PairEmote;

public final class SocialProtocol {
    public static final String REQUEST_CHANNEL = "toolvisuals:social_request_v1";
    public static final String STATE_CHANNEL = "toolvisuals:social_state_v1";
    public static final int MAX_REQUEST_BYTES = 18, MAX_STATE_BYTES = 51;
    public sealed interface Request permits Preference, Invite, Answer, Cancel {}
    public sealed interface State permits Badge, Invitation, Started, Result, Stopped {}
    public record Preference(boolean badge) implements Request {}
    public record Invite(UUID target, PairEmote emote) implements Request {
        public Invite { Objects.requireNonNull(target); Objects.requireNonNull(emote); }
    }
    public record Answer(UUID invitation, boolean accept) implements Request {
        public Answer { Objects.requireNonNull(invitation); }
    }
    public record Cancel() implements Request {}
    public record Badge(UUID player, boolean visible) implements State {
        public Badge { Objects.requireNonNull(player); }
    }
    public record Invitation(UUID id, UUID sender, PairEmote emote) implements State {
        public Invitation { Objects.requireNonNull(id); Objects.requireNonNull(sender); Objects.requireNonNull(emote); }
    }
    public record Started(UUID id, UUID first, UUID second, PairEmote emote, int age) implements State {
        public Started {
            Objects.requireNonNull(id); Objects.requireNonNull(first); Objects.requireNonNull(second); Objects.requireNonNull(emote);
            if (first.equals(second) || age < 0 || age >= emote.animation.duration) throw new IllegalArgumentException("Invalid pair state");
        }
    }
    public enum Outcome {
        SENT(0), DECLINED(1), EXPIRED(2), UNAVAILABLE(3), BUSY(4), CANCELLED(5);
        public final int id;
        Outcome(int id){this.id=id;}
        private static final java.util.Map<Integer,Outcome> IDS=ru.he3hauka.toolvisuals.animation.network.WireCatalog.index(values(),value->value.id);
    }
    public record Result(UUID id, Outcome outcome) implements State {
        public Result { Objects.requireNonNull(id); Objects.requireNonNull(outcome); }
    }
    public record Stopped(UUID id) implements State {
        public Stopped { Objects.requireNonNull(id); }
    }
    private SocialProtocol() {}

    public static byte[] encode(Request value) {
        var out = ByteBuffer.allocate(MAX_REQUEST_BYTES);
        if (value instanceof Preference v) out.put((byte) 0).put((byte) (v.badge ? 1 : 0));
        else if (value instanceof Invite v) { out.put((byte) 1); uuid(out, v.target); out.put((byte) v.emote.id); }
        else if (value instanceof Answer v) { out.put((byte) 2); uuid(out, v.invitation); out.put((byte) (v.accept ? 1 : 0)); }
        else if (value instanceof Cancel) out.put((byte) 3);
        else throw new IllegalArgumentException("Unknown request");
        return Arrays.copyOf(out.array(), out.position());
    }
    public static Request decodeRequest(byte[] bytes) {
        var in = input(bytes, MAX_REQUEST_BYTES);
        Request value = switch (Byte.toUnsignedInt(in.get())) {
            case 0 -> new Preference(bool(in));
            case 1 -> new Invite(uuid(in), emote(in));
            case 2 -> new Answer(uuid(in), bool(in));
            case 3 -> new Cancel();
            default -> throw new IllegalArgumentException("Unknown request");
        };
        end(in); return value;
    }
    public static byte[] encode(State value) {
        var out = ByteBuffer.allocate(MAX_STATE_BYTES);
        if (value instanceof Badge v) { out.put((byte) 0); uuid(out, v.player); out.put((byte) (v.visible ? 1 : 0)); }
        else if (value instanceof Invitation v) { out.put((byte) 1); uuid(out, v.id); uuid(out, v.sender); out.put((byte) v.emote.id); }
        else if (value instanceof Started v) { out.put((byte) 2); uuid(out, v.id); uuid(out, v.first); uuid(out, v.second); out.put((byte) v.emote.id).put((byte) v.age); }
        else if (value instanceof Result v) { out.put((byte) 3); uuid(out, v.id); out.put((byte) v.outcome.id); }
        else if (value instanceof Stopped v) { out.put((byte) 4); uuid(out, v.id); }
        else throw new IllegalArgumentException("Unknown state");
        return Arrays.copyOf(out.array(), out.position());
    }
    public static State decodeState(byte[] bytes) {
        var in = input(bytes, MAX_STATE_BYTES);
        State value = switch (Byte.toUnsignedInt(in.get())) {
            case 0 -> new Badge(uuid(in), bool(in));
            case 1 -> new Invitation(uuid(in), uuid(in), emote(in));
            case 2 -> new Started(uuid(in), uuid(in), uuid(in), emote(in), Byte.toUnsignedInt(in.get()));
            case 3 -> new Result(uuid(in), outcome(in));
            case 4 -> new Stopped(uuid(in));
            default -> throw new IllegalArgumentException("Unknown state");
        };
        end(in); return value;
    }
    public static UUID key(State value) {
        if (value instanceof Badge v) return v.player;
        if (value instanceof Invitation v) return v.id;
        if (value instanceof Started v) return v.id;
        if (value instanceof Result v) return v.id;
        return ((Stopped) value).id;
    }
    private static ByteBuffer input(byte[] bytes, int maximum) {
        if (bytes == null || bytes.length < 1 || bytes.length > maximum) throw new IllegalArgumentException("Invalid payload size");
        return ByteBuffer.wrap(bytes);
    }
    private static void end(ByteBuffer input) { if (input.hasRemaining()) throw new IllegalArgumentException("Trailing data"); }
    private static boolean bool(ByteBuffer in) {
        int value = Byte.toUnsignedInt(in.get());
        if (value > 1) throw new IllegalArgumentException("Invalid boolean");
        return value == 1;
    }
    private static UUID uuid(ByteBuffer in) { return new UUID(in.getLong(), in.getLong()); }
    private static void uuid(ByteBuffer out, UUID value) { out.putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits()); }
    private static PairEmote emote(ByteBuffer in) {
        var value = PairEmote.byId(Byte.toUnsignedInt(in.get()));
        if (value == null) throw new IllegalArgumentException("Unknown paired emote");
        return value;
    }
    private static Outcome outcome(ByteBuffer in) {
        int value = Byte.toUnsignedInt(in.get());
        var outcome=Outcome.IDS.get(value);
        if (outcome == null) throw new IllegalArgumentException("Unknown outcome");
        return outcome;
    }
}
