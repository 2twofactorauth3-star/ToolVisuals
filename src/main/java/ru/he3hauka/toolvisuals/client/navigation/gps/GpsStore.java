package ru.he3hauka.toolvisuals.client.navigation.gps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import ru.he3hauka.toolvisuals.client.storage.JsonFile;

public final class GpsStore {
    public static final int LIMIT = 256;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final JsonFile file;
    private volatile List<GpsMark> marks = List.of();

    public GpsStore(Path path) { file = new JsonFile(path, 768 * 1024); }
    public List<GpsMark> all() { return marks; }
    public List<GpsMark> entries(String context) { return marks.stream().filter(mark -> mark.context().equals(context)).toList(); }

    public synchronized void load() throws IOException {
        var loaded = file.read(json -> {
            var saved = GSON.fromJson(json, Saved.class);
            if (saved == null || saved.version != 1 || saved.marks == null || saved.marks.size() > LIMIT)
                throw new IOException("Invalid GPS marker file");
            var ids = new HashSet<UUID>();
            for (var mark : saved.marks) if (mark == null || !ids.add(mark.id())) throw new IOException("Duplicate GPS marker");
            return List.copyOf(saved.marks);
        });
        if (loaded != null) marks = loaded;
    }

    public synchronized void put(GpsMark mark, boolean selectOnly) throws IOException {
        var next = new ArrayList<GpsMark>();
        boolean found = false;
        for (var old : marks) {
            if (old.id().equals(mark.id())) { next.add(mark); found = true; }
            else next.add(selectOnly && old.context().equals(mark.context()) ? old.active(false) : old);
        }
        if (!found) next.add(mark);
        write(next);
    }

    public synchronized void selectOnly(String context, UUID id) throws IOException {
        if (marks.stream().noneMatch(mark -> mark.context().equals(context) && mark.id().equals(id)))
            throw new IllegalArgumentException("Unknown GPS marker");
        write(marks.stream().map(mark -> mark.context().equals(context) ? mark.active(mark.id().equals(id)) : mark).toList());
    }

    public synchronized void selectAll(String context, boolean active) throws IOException {
        write(marks.stream().map(mark -> (context.isEmpty() || mark.context().equals(context)) ? mark.active(active) : mark).toList());
    }

    public synchronized void toggle(UUID id) throws IOException {
        write(marks.stream().map(mark -> mark.id().equals(id) ? mark.active(!mark.active()) : mark).toList());
    }

    public synchronized void remove(UUID id) throws IOException {
        write(marks.stream().filter(mark -> !mark.id().equals(id)).toList());
    }

    private void write(List<GpsMark> next) throws IOException {
        if (next.size() > LIMIT) throw new IllegalStateException("GPS marker limit reached");
        file.write(GSON.toJson(new Saved(1, next)));
        marks = List.copyOf(next);
    }

    private record Saved(int version, List<GpsMark> marks) {}
}
