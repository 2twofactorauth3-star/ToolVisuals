package ru.he3hauka.toolvisuals.client.storage;

import ru.he3hauka.toolvisuals.storage.AtomicFiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

public final class JsonFile {
    public enum Status { READY, DIRTY, LOAD_FAILED, WRITE_FAILED }
    @FunctionalInterface public interface Decoder<T> { T decode(JsonElement json) throws IOException; }
    public final Path path;
    private final BoundedJson.Limits limits;
    private boolean attempted, protectedOriginal;
    private volatile Status status = Status.READY;
    private volatile IOException failure;
    private long writes;

    public JsonFile(Path path) { this(path, BoundedJson.MAX_BYTES); }
    public JsonFile(Path path, int maximum) {
        this(path, standardLimits(maximum));
    }
    public JsonFile(Path path, BoundedJson.Limits limits) {
        this.path = path.toAbsolutePath().normalize(); this.limits = java.util.Objects.requireNonNull(limits);
    }
    private static BoundedJson.Limits standardLimits(int maximum){
        if(maximum<1||maximum>BoundedJson.MAX_BYTES)throw new IllegalArgumentException("Invalid JSON file limit");
        return new BoundedJson.Limits(maximum,BoundedJson.DEFAULT.tokens());
    }
    public Status status() { return status; }
    public IOException failure() { return failure; }
    public synchronized long writes() { return writes; }
    public void dirty() { if (status == Status.READY) status = Status.DIRTY; }

    public synchronized <T> T read(Decoder<T> decoder) throws IOException {
        attempted = true;
        try {
            if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) { protectedOriginal = false; status = Status.READY; failure = null; return null; }
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Configuration is not a regular file");
            if (Files.size(path) > limits.bytes()) throw new IOException("Configuration exceeds its readable size limit");
            T value = decoder.decode(BoundedJson.read(path, limits));
            protectedOriginal = false; status = Status.READY; failure = null;
            return value;
        } catch (IOException | RuntimeException error) {
            protectedOriginal = true; status = Status.LOAD_FAILED;
            failure = new IOException("Unreadable configuration; original file preserved", error);
            ClientErrors.report("Read configuration "+path.getFileName(),failure);throw failure;
        }
    }

    public synchronized void write(String json) throws IOException {
        if (protectedOriginal || !attempted && !Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
            status = Status.LOAD_FAILED;
            failure = new IOException("Read or repair the original configuration before saving");
            ClientErrors.report("Save protected configuration "+path.getFileName(),failure);throw failure;
        }
        try {
            write(path, json, limits); writes++; attempted = true; status = Status.READY; failure = null;
        } catch (IOException | RuntimeException error) {
            status = Status.WRITE_FAILED;failure = error instanceof IOException io ? io : new IOException("Cannot encode configuration", error);
            ClientErrors.report("Save configuration "+path.getFileName(),failure);throw failure;
        }
    }

    public void writeSnapshot(java.util.function.Supplier<String> snapshot) throws IOException {
        final String json;
        try { json = snapshot.get(); }
        catch (RuntimeException error) {
            synchronized (this) {
                if (status != Status.LOAD_FAILED) { status = Status.WRITE_FAILED; failure = new IOException("Cannot encode configuration", error); }
                ClientErrors.report("Encode configuration "+path.getFileName(),error);
                throw failure;
            }
        }
        write(json);
    }

    public static void write(Path path, String json, int maximum) throws IOException {
        if (maximum < 1 || maximum > BoundedJson.MAX_BYTES) throw new IOException("Invalid JSON snapshot or limit");
        try{write(path, json, new BoundedJson.Limits(maximum, BoundedJson.DEFAULT.tokens()));}
        catch(IOException error){ClientErrors.report("Export configuration "+path.getFileName(),error);throw error;}
    }
    private static void write(Path path, String json, BoundedJson.Limits limits) throws IOException {
        if (json == null) throw new IOException("Invalid JSON snapshot");
        Path target = path.toAbsolutePath().normalize();
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > limits.bytes()) throw new IOException("Saved configuration exceeds its readable size limit");
        try { BoundedJson.parse(json, limits); }
        catch (RuntimeException error) { throw new IOException("Saved configuration exceeds its readable JSON limits", error); }
        AtomicFiles.write(target,bytes);
    }
}
