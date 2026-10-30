package ru.he3hauka.toolvisuals.network.world;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharacterCodingException;
import java.util.UUID;

public final class WorldContextWire {
    public static final String CHANNEL = "toolvisuals:world_v1";
    public static final String EXTENDED_CHANNEL = "toolvisuals:world_v2";
    public static final String NAMED_CHANNEL = "toolvisuals:world_v3";
    public static final int BYTES = 18;
    public static final int MAX_NAME_BYTES = 384;
    public static final int MAX_NAMED_BYTES = 20 + MAX_NAME_BYTES;

    public enum Area {
        OTHER(0), SPAWN(1), ARENA(2);

        public final int id;

        Area(int id) { this.id = id; }

        public static Area fromName(String name) {
            if ("spawn".equals(name)) return SPAWN;
            if ("arena".equals(name)) return ARENA;
            return OTHER;
        }

        private static Area fromId(int id) {
            return switch (id) {
                case 0 -> OTHER;
                case 1 -> SPAWN;
                case 2 -> ARENA;
                default -> throw new IllegalArgumentException("Invalid world area");
            };
        }
    }

    public record State(UUID world, Area area, String name) {
        public State {
            java.util.Objects.requireNonNull(world);
            java.util.Objects.requireNonNull(area);
            if (!validName(name))
                throw new IllegalArgumentException("Invalid world name");
        }

        public State(UUID world, Area area) { this(world, area, ""); }
        public State(UUID world, boolean spawn) { this(world, spawn ? Area.SPAWN : Area.OTHER); }
        public boolean spawn() { return area == Area.SPAWN; }
    }

    private WorldContextWire() {}

    public static boolean validName(String name) {
        return name != null && name.length() <= 128 && name.equals(name.strip())
                && name.codePoints().noneMatch(value -> Character.isISOControl(value) || value >= 0xd800 && value <= 0xdfff)
                && name.getBytes(StandardCharsets.UTF_8).length <= MAX_NAME_BYTES;
    }

    public static byte[] encode(State state) { return encode(state, 1); }
    public static byte[] encodeExtended(State state) { return encode(state, 2); }
    public static State decode(byte[] bytes) { return decode(bytes, 1); }
    public static State decodeExtended(byte[] bytes) { return decode(bytes, 2); }

    public static byte[] encodeNamed(State state) {
        var name = state.name.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.allocate(20 + name.length).put((byte)3).putLong(state.world.getMostSignificantBits())
                .putLong(state.world.getLeastSignificantBits()).put((byte)state.area.id).putShort((short)name.length).put(name).array();
    }

    public static State decodeNamed(byte[] bytes) {
        if (bytes.length < 20 || bytes.length > MAX_NAMED_BYTES) throw new IllegalArgumentException("Invalid named world context length");
        var buffer = ByteBuffer.wrap(bytes);
        if (buffer.get() != 3) throw new IllegalArgumentException("Unsupported named world context");
        var world = new UUID(buffer.getLong(), buffer.getLong());
        var area = Area.fromId(Byte.toUnsignedInt(buffer.get()));
        int length = Short.toUnsignedInt(buffer.getShort());
        if (length != buffer.remaining()) throw new IllegalArgumentException("Invalid world name length");
        try {
            String name = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(buffer).toString();
            return new State(world, area, name);
        } catch (CharacterCodingException error) {
            throw new IllegalArgumentException("Invalid world name encoding", error);
        }
    }

    private static byte[] encode(State state, int version) {
        int flag = version == 1 ? state.spawn() ? 1 : 0 : state.area.id;
        return ByteBuffer.allocate(BYTES).put((byte)version).putLong(state.world.getMostSignificantBits())
                .putLong(state.world.getLeastSignificantBits()).put((byte)flag).array();
    }

    private static State decode(byte[] bytes, int version) {
        if (bytes.length != BYTES) throw new IllegalArgumentException("Invalid world context length");
        var buffer = ByteBuffer.wrap(bytes);
        if (buffer.get() != version) throw new IllegalArgumentException("Unsupported world context");
        var world = new UUID(buffer.getLong(), buffer.getLong());
        int flag = Byte.toUnsignedInt(buffer.get());
        if (version == 1 && flag > 1) throw new IllegalArgumentException("Invalid world context flag");
        return new State(world, Area.fromId(flag));
    }
}
