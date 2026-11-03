package ru.he3hauka.toolvisuals.client.sound.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import com.mojang.blaze3d.audio.SoundBuffer;
import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.Channel;
import ru.he3hauka.toolvisuals.client.storage.JsonFile;
import ru.he3hauka.toolvisuals.storage.AtomicFiles;

public final class SoundLibrary {
    public static final int LIMIT = 64;
    public static final long BYTE_BUDGET = 32L * 1024 * 1024;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path directory;
    private final JsonFile file;
    private volatile List<Entry> entries = List.of();
    private volatile Map<String, String> selection = Map.of();

    public record Entry(String id, int rate, int channels, int bytes, String hash) {
        public Entry {
            if (!SoundIds.valid(id) || rate < 8000 || rate > 96000 || channels < 1 || channels > 2
                    || bytes <= 0 || bytes % (channels * 2) != 0 || bytes > rate * channels * 2 * AudioDecoder.MAX_SECONDS
                    || hash == null || !hash.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid sound metadata");
        }
        public float seconds() { return bytes / (float)(rate * channels * 2); }
    }
    private record Saved(int version, List<Entry> entries, Map<String, String> selection) {}

    public SoundLibrary(Path directory) { this.directory = directory.toAbsolutePath().normalize(); file = new JsonFile(this.directory.resolve("index.json")); }
    public List<Entry> entries() { return entries; }
    public Entry get(String id) { return entries.stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null); }
    public String selected(Channel channel) { return selection.get(channel.name()); }
    public boolean availableId(String input) { String id = SoundIds.normalize(input); return SoundIds.valid(id) && get(id) == null; }

    public synchronized void load() throws IOException {
        var saved = file.read(json -> {
            var value = GSON.fromJson(json, Saved.class);
            if (value == null || value.version != 1 || value.entries == null || value.entries.size() > LIMIT || value.selection == null)
                throw new IOException("Invalid sound library");
            var ids = new java.util.HashSet<String>(); long total = 0;
            for (var entry : value.entries) { if (entry == null || !ids.add(entry.id())) throw new IOException("Duplicate sound ID"); total += entry.bytes(); }
            if (total > BYTE_BUDGET) throw new IOException("Sound library exceeds its byte budget");
            for (var selected : value.selection.entrySet()) {
                try { Channel.valueOf(selected.getKey()); } catch (RuntimeException error) { throw new IOException("Unknown sound channel", error); }
                if (!ids.contains(selected.getValue())) throw new IOException("Unknown selected sound");
            }
            return value;
        });
        if (saved != null) { entries = List.copyOf(saved.entries); selection = Map.copyOf(saved.selection); }
    }

    public synchronized Entry importSound(String input, DecodedSound sound) throws IOException {
        String id = SoundIds.require(input);
        if (!availableId(id)) throw new IllegalArgumentException("Sound ID already exists");
        if (entries.size() >= LIMIT || entries.stream().mapToLong(Entry::bytes).sum() + sound.pcm().length > BYTE_BUDGET)
            throw new IllegalStateException("Sound library limit reached");
        var entry = new Entry(id, Math.round(sound.format().getSampleRate()), sound.format().getChannels(), sound.pcm().length, hash(sound.pcm()));
        Path data = data(id);
        if (Files.exists(data)) throw new IOException("A sound file with this ID already exists");
        AtomicFiles.write(data, sound.pcm());
        var next = new ArrayList<>(entries); next.add(entry);
        try { write(next, selection); }
        catch (IOException | RuntimeException error) {
            try { Files.deleteIfExists(data); } catch (IOException cleanup) { error.addSuppressed(cleanup); }
            throw error;
        }
        return entry;
    }

    public synchronized void assign(Channel channel, String id) throws IOException {
        if (get(id) == null) throw new IllegalArgumentException("Unknown sound ID");
        var next = new LinkedHashMap<>(selection); next.put(channel.name(), id); write(entries, next);
    }

    public synchronized void remove(String id) throws IOException {
        if (get(id) == null) return;
        var next = entries.stream().filter(entry -> !entry.id().equals(id)).toList();
        var selected = new LinkedHashMap<>(selection); selected.values().removeIf(id::equals);
        write(next, selected);
        try { Files.deleteIfExists(data(id)); }
        catch (IOException error) { ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors.report("Remove custom sound file " + id, error); throw error; }
    }

    public SoundBuffer buffer(String id, String revision) throws IOException {
        var entry = get(id);
        if (entry == null || !entry.hash().equals(revision)) throw new IOException("Unknown sound revision");
        Path path = data(id);
        if (!Files.isRegularFile(path) || Files.size(path) != entry.bytes()) throw new IOException("Custom sound file is missing or changed");
        byte[] pcm = Files.readAllBytes(path);
        if (!hash(pcm).equals(entry.hash())) throw new IOException("Custom sound checksum mismatch");
        ByteBuffer buffer = ByteBuffer.allocateDirect(pcm.length).put(pcm).flip();
        return new SoundBuffer(buffer, new AudioFormat(entry.rate(), 16, entry.channels(), true, false));
    }

    private void write(List<Entry> next, Map<String, String> selected) throws IOException {
        file.write(GSON.toJson(new Saved(1, next, selected))); entries = List.copyOf(next); selection = Map.copyOf(selected);
    }
    private Path data(String id) { return directory.resolve(SoundIds.require(id) + ".pcm"); }
    private static String hash(byte[] data) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException("Missing SHA-256", error); }
    }
}
