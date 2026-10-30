package ru.he3hauka.toolvisuals.client.music.backend;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import ru.he3hauka.toolvisuals.client.music.model.MediaAction;
import ru.he3hauka.toolvisuals.client.music.model.MediaSession;
import ru.he3hauka.toolvisuals.client.music.model.MediaSnapshot;
import ru.he3hauka.toolvisuals.client.music.model.MediaVolume;

public final class MediaProtocol {
    public static final int VERSION=1, MAX_LINE=786_432, MAX_ARTWORK=524_288;
    public sealed interface Event permits Ready, State, Artwork, Reply, Failure {}
    public record Ready() implements Event {}
    public record State(MediaSnapshot snapshot) implements Event {}
    public record Artwork(String id, String track, String data) implements Event {
        public String key(long generation) { return generation + "/" + id + "/" + track; }
    }
    public record Reply(long request, String status) implements Event {}
    public record Failure(String operation, String message, long code) implements Event {}
    private MediaProtocol() {}

    public static String readLine(Reader reader) throws IOException {
        var line = new StringBuilder(2048);
        for (int ch; (ch = reader.read()) != -1;) {
            if (ch == '\n') return line.toString();
            if (line.length() >= MAX_LINE) throw new IOException("Media response exceeds its line limit");
            if (ch != '\r') line.append((char)ch);
        }
        if (!line.isEmpty()) throw new IOException("Incomplete media response");
        return null;
    }

    public static Event decode(String line, long generation, long received) throws IOException {
        if (line.length() > MAX_LINE) throw new IOException("Media response exceeds its line limit");
        try (var reader = new JsonReader(new StringReader(line))) {
            reader.setStrictness(Strictness.STRICT);
            reader.beginObject();
            int fields=0;
            while (reader.hasNext()) {
                if (++fields > 8) throw new IOException("Too many media response fields");
                String name=reader.nextName();
                if (name.equals("sessions")) {
                    reader.beginArray(); int sessions=0;
                    while (reader.hasNext()) {
                        if (++sessions > 16) throw new IOException("Too many media sessions");
                        reader.beginObject(); int properties=0;
                        while (reader.hasNext()) {
                            if (++properties > 24) throw new IOException("Too many media session fields");
                            reader.nextName(); scalar(reader, 256);
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                } else scalar(reader, name.equals("data") ? (MAX_ARTWORK + 2) / 3 * 4 : name.equals("message") ? 8192 : 128);
            }
            reader.endObject();
            if (reader.peek() != com.google.gson.stream.JsonToken.END_DOCUMENT) throw new IOException("Trailing media response data");
            var json=JsonParser.parseString(line).getAsJsonObject();
            return switch (text(json,"kind")) {
                case "ready" -> {
                    if (json.get("version").getAsInt() != VERSION) throw new IOException("Unsupported media bridge protocol");
                    yield new Ready();
                }
                case "state" -> new State(snapshot(json, generation, received));
                case "art" -> new Artwork(text(json,"id"), text(json,"track"), text(json,"data"));
                case "reply" -> new Reply(number(json,"request"), text(json,"status"));
                case "error" -> new Failure(text(json,"operation"), text(json,"message"), number(json,"code"));
                default -> throw new IOException("Unknown media bridge message");
            };
        } catch (RuntimeException error) { throw new IOException("Invalid media bridge message", error); }
    }
    private static void scalar(JsonReader reader, int maximum) throws IOException {
        switch (reader.peek()) {
            case STRING, NUMBER -> { if (reader.nextString().length() > maximum) throw new IOException("Media field exceeds its size limit"); }
            case BOOLEAN -> reader.nextBoolean();
            case NULL -> reader.nextNull();
            default -> throw new IOException("Nested media field");
        }
    }
    private static MediaSnapshot snapshot(JsonObject json, long generation, long received) throws IOException {
        var sessions=new ArrayList<MediaSession>(); var ids=new HashSet<String>();
        for (var value : json.getAsJsonArray("sessions")) {
            var item=value.getAsJsonObject(); String id=text(item,"id");
            if (!id.matches("[0-9]{1,10}") || !ids.add(id)) throw new IOException("Invalid or duplicate media session");
            sessions.add(new MediaSession(id,text(item,"app"),text(item,"title"),text(item,"artist"),text(item,"album"),text(item,"track"),text(item,"state"),
                    number(item,"position"),number(item,"start"),number(item,"end"),number(item,"minimum"),number(item,"maximum"),
                    item.get("rate").getAsDouble(),item.get("shuffle").getAsBoolean(),item.get("repeat").getAsInt(),item.get("capabilities").getAsInt(),
                    item.has("volume")?new MediaVolume(item.get("volume").getAsDouble(),item.get("muted").getAsBoolean(),text(item,"volumeTarget"),text(item,"volumeScope")):MediaVolume.UNAVAILABLE));
        }
        String current=text(json,"current"), pinned=text(json,"pinned");
        if (!current.isEmpty() && !ids.contains(current) || !pinned.isEmpty() && !ids.contains(pinned)) throw new IOException("Missing current media session");
        return new MediaSnapshot(generation, received, current, pinned, sessions);
    }
    public static String command(long request, MediaAction action, String session, String track, String target, double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite media control value");
        var json=new JsonObject(); json.addProperty("request",request); json.addProperty("action",action.wire);
        json.addProperty("session",session); json.addProperty("track",track); json.addProperty("target",target); json.addProperty("value",value);
        return json.toString();
    }
    private static String text(JsonObject json,String key) { return json.get(key).getAsString(); }
    private static long number(JsonObject json,String key) { return json.get(key).getAsLong(); }
}
