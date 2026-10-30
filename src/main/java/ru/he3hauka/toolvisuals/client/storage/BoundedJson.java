package ru.he3hauka.toolvisuals.client.storage;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public final class BoundedJson {
    public static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final int MAX_DEPTH = 32, MAX_TOKENS = 65_536, MAX_STRING = 32_768;
    public record Limits(int bytes, int tokens) {
        public Limits { if (bytes < 1 || bytes > 16 * 1024 * 1024 || tokens < 1 || tokens > 131_072) throw new IllegalArgumentException("Invalid JSON limits"); }
    }
    public static final Limits DEFAULT = new Limits(MAX_BYTES, MAX_TOKENS);

    private BoundedJson() {}

    public static JsonElement read(Path path) throws IOException {
        return read(path, DEFAULT);
    }
    public static JsonElement read(Path path, Limits limits) throws IOException {
        try (var stream = new BoundedInputStream(Files.newInputStream(path), limits.bytes)) {
            var text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(stream.readAllBytes()));
            return parse(text.toString(), limits);
        }
    }

    public static JsonElement parse(String text) {
        return parse(text, DEFAULT);
    }
    public static JsonElement parse(String text, Limits limits) {
        if (text == null || text.length() > limits.bytes) throw new JsonParseException("JSON exceeds the size limit");
        if (text.getBytes(StandardCharsets.UTF_8).length > limits.bytes) throw new JsonParseException("JSON exceeds the size limit");
        try (var reader = new JsonReader(new StringReader(text))) {
            reader.setStrictness(Strictness.STRICT);
            int depth = 0, tokens = 0;
            while (reader.peek() != JsonToken.END_DOCUMENT) {
                if (++tokens > limits.tokens) throw new JsonParseException("Too many JSON values");
                switch (reader.peek()) {
                    case BEGIN_ARRAY, BEGIN_OBJECT -> {
                        if (++depth > MAX_DEPTH) throw new JsonParseException("JSON nesting exceeds the limit");
                        if (reader.peek() == JsonToken.BEGIN_ARRAY) reader.beginArray(); else reader.beginObject();
                    }
                    case END_ARRAY -> { reader.endArray(); depth--; }
                    case END_OBJECT -> { reader.endObject(); depth--; }
                    case NAME -> checkString(reader.nextName(), MAX_STRING);
                    case STRING -> checkString(reader.nextString(), MAX_STRING);
                    case NUMBER -> checkString(reader.nextString(), 128);
                    case BOOLEAN -> reader.nextBoolean();
                    case NULL -> reader.nextNull();
                    default -> throw new JsonParseException("Invalid JSON token");
                }
            }
            if (tokens == 0 || depth != 0) throw new JsonParseException("Incomplete JSON document");
        } catch (IOException error) {
            throw new JsonParseException("Invalid JSON document", error);
        }
        return JsonParser.parseString(text);
    }

    private static void checkString(String value, int limit) {
        if (value.length() > limit) throw new JsonParseException("JSON value exceeds the length limit");
    }
}
