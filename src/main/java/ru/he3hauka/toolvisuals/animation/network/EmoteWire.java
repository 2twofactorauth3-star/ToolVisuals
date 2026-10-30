package ru.he3hauka.toolvisuals.animation.network;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import ru.he3hauka.toolvisuals.animation.Emote;

public final class EmoteWire {
    public static final String REQUEST_CHANNEL = "toolvisuals:emote_request_v1";
    public static final String STATE_CHANNEL = "toolvisuals:emote_state_v1";
    public static final String EXTENDED_REQUEST_CHANNEL = "toolvisuals:emote_request_v2";
    public static final String EXTENDED_STATE_CHANNEL = "toolvisuals:emote_state_v2";
    public static final int MAX_REQUEST_BYTES = 6, MAX_STATE_BYTES = 22;

    public record Request(int emote, boolean cancelMoving) {
        public Request {
            if (!validRequest(emote)) throw new IllegalArgumentException("Unknown emote");
        }
    }

    public record State(UUID player, int emote, int age) {
        public State {
            Objects.requireNonNull(player);
            if (!validState(emote, age)) throw new IllegalArgumentException("Invalid emote state");
        }
    }

    private EmoteWire() {}

    public static boolean validRequest(int id) { return id == -1 || Emote.byId(id) != null; }
    public static State forRecipient(State state, boolean extended) {
        return !extended && state.emote >= Emote.LEGACY_COUNT ? new State(state.player, -1, 0) : state;
    }
    public static boolean validState(int id, int age) {
        var emote = Emote.byId(id);
        return id == -1 ? age == 0 : emote != null && age >= 0 && age < emote.duration;
    }

    public static Request decodeRequest(byte[] bytes) {
        var input = input(bytes, 2, MAX_REQUEST_BYTES);
        int id = varInt(input), moving = Byte.toUnsignedInt(input.get());
        if (input.hasRemaining() || moving > 1) throw new IllegalArgumentException("Invalid emote request");
        return new Request(id, moving == 1);
    }

    public static State decodeState(byte[] bytes) {
        var input = input(bytes, 18, MAX_STATE_BYTES);
        var player = new UUID(input.getLong(), input.getLong());
        var state = new State(player, varInt(input), varInt(input));
        if (input.hasRemaining()) throw new IllegalArgumentException("Trailing emote data");
        return state;
    }

    public static byte[] encode(Request request) {
        var output = ByteBuffer.allocate(MAX_REQUEST_BYTES);
        varInt(output, request.emote());
        output.put((byte) (request.cancelMoving() ? 1 : 0));
        return Arrays.copyOf(output.array(), output.position());
    }

    public static byte[] encode(State state) {
        var output = ByteBuffer.allocate(MAX_STATE_BYTES);
        output.putLong(state.player().getMostSignificantBits()).putLong(state.player().getLeastSignificantBits());
        varInt(output, state.emote());
        varInt(output, state.age());
        return Arrays.copyOf(output.array(), output.position());
    }

    private static ByteBuffer input(byte[] bytes, int minimum, int maximum) {
        if (bytes == null || bytes.length < minimum || bytes.length > maximum) throw new IllegalArgumentException("Invalid emote payload size");
        return ByteBuffer.wrap(bytes);
    }

    private static int varInt(ByteBuffer input) {
        int value = 0;
        for (int index = 0; index < 5; index++) {
            int part = Byte.toUnsignedInt(input.get());
            if (index == 4 && (part & 0xf0) != 0) throw new IllegalArgumentException("Invalid VarInt");
            value |= (part & 0x7f) << index * 7;
            if ((part & 0x80) == 0) return value;
        }
        throw new IllegalArgumentException("Invalid VarInt");
    }

    private static void varInt(ByteBuffer output, int value) {
        do {
            int part = value & 0x7f;
            value >>>= 7;
            output.put((byte) (part | (value == 0 ? 0 : 0x80)));
        } while (value != 0);
    }
}
