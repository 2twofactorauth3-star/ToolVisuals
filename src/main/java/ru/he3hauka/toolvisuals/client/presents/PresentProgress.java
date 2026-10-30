package ru.he3hauka.toolvisuals.client.presents;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.he3hauka.toolvisuals.client.storage.JsonFile;

public final class PresentProgress {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<Integer> IDS = PresentCatalog.ALL.stream().map(Present::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
    private final JsonFile file;
    private Map<String, Set<Integer>> collected = Map.of();

    public PresentProgress(Path path) { file = new JsonFile(path, 256 * 1024); }

    public void load() throws IOException {
        var loaded = file.read(json -> {
            var saved = GSON.fromJson(json, Saved.class);
            if (saved == null || (saved.version != 1 && saved.version != 2) || saved.servers == null || saved.servers.size() > 128)
                throw new IOException("Invalid present progress");
            var next = new HashMap<String, Set<Integer>>();
            for (var entry : saved.servers.entrySet()) {
                validate(entry.getKey());
                if (entry.getValue() == null || entry.getValue().size() > IDS.size() || !IDS.containsAll(entry.getValue()))
                    throw new IOException("Invalid collected presents");
                next.put(entry.getKey(), Set.copyOf(entry.getValue()));
            }
            return Map.copyOf(next);
        });
        if (loaded != null) collected = loaded;
    }

    public Set<Integer> collected(String server) { return collected.getOrDefault(server, Set.of()); }

    public boolean collect(String server, Present present) throws IOException {
        validate(server);
        if (!PresentCatalog.ALL.contains(present)) throw new IllegalArgumentException("Unknown present");
        var points = new HashSet<>(collected(server));
        if (!points.add(present.id())) return false;
        var next = new HashMap<>(collected);
        next.put(server, Set.copyOf(points));
        write(next);
        return true;
    }

    public void reset(String server) throws IOException {
        validate(server);
        var next = new HashMap<>(collected); next.remove(server); write(next);
    }

    private void write(Map<String, Set<Integer>> next) throws IOException {
        if (next.size() > 128) throw new IOException("Present progress server limit reached");
        var saved = new Saved(); saved.version = 2; saved.servers = next;
        file.write(GSON.toJson(saved));
        collected = Map.copyOf(next);
    }

    private static void validate(String server) {
        if (server == null || server.isBlank() || server.length() > 2048 || server.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid present progress context");
    }

    private static final class Saved { int version; Map<String, Set<Integer>> servers; }
}
