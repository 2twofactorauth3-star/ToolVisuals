package ru.he3hauka.toolvisuals.client.connection.holyworld;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.gson.JsonObject;

import ru.he3hauka.toolvisuals.client.connection.policy.FeatureCatalog;
import ru.he3hauka.toolvisuals.client.storage.BoundedJson;

public final class HolyWorldProtocol {
    public static final String CHANNEL = "liteapi:feature-control";
    public static final String CLIENT_ID = "toolvisuals";
    public static final int MAX_BYTES = 32_768;
    private static final int MAX_FEATURES = 512, MAX_ID = 96;
    private static final BoundedJson.Limits LIMITS = new BoundedJson.Limits(MAX_BYTES, 4096);

    public record Response(String id, boolean ok, List<String> blocked, String error) {}

    private HolyWorldProtocol() {}

    public static byte[] request(String id) {
        var request = new JsonObject();
        request.addProperty("id", id);
        request.addProperty("method", "checkFeatures");
        var payload = new JsonObject();
        payload.addProperty("client", CLIENT_ID);
        var features = new com.google.gson.JsonArray();
        FeatureCatalog.IDS.forEach(features::add);
        payload.add("features", features);
        request.add("payload", payload);
        byte[] bytes = request.toString().getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) throw new IllegalStateException("Feature catalog exceeds the protocol budget");
        return bytes;
    }

    public static Response response(byte[] bytes) {
        if (bytes.length == 0 || bytes.length > MAX_BYTES) throw new IllegalArgumentException("Invalid feature response size");
        final String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException error) {
            throw new IllegalArgumentException("Invalid feature response encoding", error);
        }
        var json = BoundedJson.parse(text, LIMITS);
        if (!json.isJsonObject()) throw new IllegalArgumentException("Feature response must be an object");
        var object = json.getAsJsonObject();
        String id = string(object, "id", MAX_ID);
        var ok = object.get("ok");
        if (ok == null || !ok.isJsonPrimitive() || !ok.getAsJsonPrimitive().isBoolean())
            throw new IllegalArgumentException("Invalid feature response status");
        if (!ok.getAsBoolean()) return new Response(id, false, List.of(),
                object.has("error") && !object.get("error").isJsonNull() ? string(object, "error", 512) : "unknown error");
        var payload = object.get("payload");
        if (payload == null || !payload.isJsonObject()) throw new IllegalArgumentException("Invalid feature response payload");
        var blocked = payload.getAsJsonObject().get("blocklist");
        if (blocked == null || !blocked.isJsonArray() || blocked.getAsJsonArray().size() > MAX_FEATURES)
            throw new IllegalArgumentException("Invalid feature blocklist");
        var ids = new LinkedHashSet<String>();
        for (var entry : blocked.getAsJsonArray()) {
            if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()
                    || entry.getAsString().isBlank() || entry.getAsString().length() > MAX_ID)
                throw new IllegalArgumentException("Invalid blocked feature ID");
            ids.add(entry.getAsString());
        }
        return new Response(id, true, List.copyOf(ids), "");
    }

    private static String string(JsonObject object, String name, int maximum) {
        var value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                || value.getAsString().isBlank() || value.getAsString().length() > maximum)
            throw new IllegalArgumentException("Invalid feature response " + name);
        return value.getAsString();
    }
}
