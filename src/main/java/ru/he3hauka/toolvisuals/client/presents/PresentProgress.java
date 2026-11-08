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
    private Map<String, Map<String, Set<Integer>>> catalogs = Map.of();

    public PresentProgress(Path path) { file = new JsonFile(path, 256 * 1024); }

    public void load() throws IOException {
        var loaded = file.read(json -> {
            var saved = GSON.fromJson(json, Saved.class);
            if (saved == null) throw new IOException("Invalid present progress");
            if (saved.version == 1 || saved.version == 2)
                return Map.of("legacy-2026-10", validateServers(saved.servers));
            if (saved.version != 3 || saved.catalogs == null || saved.catalogs.size() > 8)
                throw new IOException("Invalid present catalogs");
            var next = new HashMap<String, Map<String, Set<Integer>>>();
            for (var entry : saved.catalogs.entrySet()) {
                if (entry.getKey() == null || !entry.getKey().matches("[a-z0-9][a-z0-9_.-]{0,63}"))
                    throw new IOException("Invalid present catalog");
                next.put(entry.getKey(), validateServers(entry.getValue()));
            }
            return Map.copyOf(next);
        });
        if (loaded != null) catalogs = loaded;
    }

    private static Map<String, Set<Integer>> validateServers(Map<String, Set<Integer>> servers) throws IOException {
        if (servers == null || servers.size() > 128) throw new IOException("Invalid present progress");
        var next = new HashMap<String, Set<Integer>>();
        for (var entry : servers.entrySet()) {
            validate(entry.getKey());
            if (entry.getValue() == null || entry.getValue().size() > IDS.size() || !IDS.containsAll(entry.getValue()))
                throw new IOException("Invalid collected presents");
            next.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        return Map.copyOf(next);
    }

    private Map<String, Set<Integer>> current() { return catalogs.getOrDefault(PresentCatalog.ID, Map.of()); }
    public Set<Integer> collected(String server) { return current().getOrDefault(server, Set.of()); }

    public boolean collect(String server, Present present) throws IOException {
        validate(server);
        if (!PresentCatalog.ALL.contains(present)) throw new IllegalArgumentException("Unknown present");
        var points = new HashSet<>(collected(server));
        if (!points.add(present.id())) return false;
        var next = new HashMap<>(current());
        next.put(server, Set.copyOf(points));
        write(next);
        return true;
    }

    public void reset(String server) throws IOException {
        validate(server);
        var next = new HashMap<>(current()); next.remove(server); write(next);
    }

    private void write(Map<String, Set<Integer>> next) throws IOException {
        if (next.size() > 128) throw new IOException("Present progress server limit reached");
        var updated = new HashMap<>(catalogs);
        updated.put(PresentCatalog.ID, Map.copyOf(next));
        if (updated.size() > 8) throw new IOException("Present catalog limit reached");
        var saved = new Saved(); saved.version = 3; saved.catalogs = updated;
        file.write(GSON.toJson(saved));
        catalogs = Map.copyOf(updated);
    }

    private static void validate(String server) {
        if (server == null || server.isBlank() || server.length() > 2048 || server.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid present progress context");
    }

    private static final class Saved {
        int version;
        Map<String, Set<Integer>> servers;
        Map<String, Map<String, Set<Integer>>> catalogs;
    }
}
