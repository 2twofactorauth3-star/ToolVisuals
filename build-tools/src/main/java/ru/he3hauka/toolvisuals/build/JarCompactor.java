package ru.he3hauka.toolvisuals.build;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import ru.he3hauka.toolvisuals.build.compatibility.ViaFabricPlusCompatibility;
import ru.he3hauka.toolvisuals.client.compatibility.lunar.LunarChatCompatibility;

public final class JarCompactor {
    private record Entry(String name, byte[] bytes, long timestamp) {}
    private static final Gson JSON = new Gson();
    private static final String METADATA = "fabric.mod.json";
    private final Map<String, String> provided = new HashMap<>();
    private final java.util.Set<String> preserved = new HashSet<>();
    private int removed;
    private int compatibilityConfigurations;
    private int expectedCompatibilityConfigurations;

    private JarCompactor(byte[] apiBytes) throws IOException {
        var api = entries(apiBytes);
        var metadata = metadata(api);
        if (metadata == null || !metadata.get("id").getAsString().equals("fabric-api")) throw new IOException("Expected the full Fabric API distribution");
        for (var value : metadata.getAsJsonArray("jars")) {
            var path = value.getAsJsonObject().get("file").getAsString();
            var child = metadata(entries(required(api, path).bytes));
            if (child == null) throw new IOException("Fabric API module has no metadata: " + path);
            provided.put(child.get("id").getAsString(), child.get("version").getAsString());
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected distribution JAR and Fabric API JAR");
        var target = Path.of(args[0]);
        MixinPackages.verify(target);
        var tool = new JarCompactor(Files.readAllBytes(Path.of(args[1])));
        var before = Files.readAllBytes(target);
        var after = tool.compact(before, 0);
        if(tool.compatibilityConfigurations!=tool.expectedCompatibilityConfigurations)throw new IOException("Unexpected ViaFabricPlus compatibility plugin count");
        if (after.length >= before.length) throw new IOException("Compacted distribution is not smaller");
        var temporary = target.resolveSibling(target.getFileName() + ".compact.tmp");
        Files.write(temporary, after);
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        System.out.printf(Locale.ROOT, "COMPACT_JAR_OK %d -> %d bytes (%.2f%% smaller), %d duplicate Fabric API modules removed%n", before.length, after.length, (before.length - after.length) * 100d / before.length, tool.removed);
    }

    private byte[] compact(byte[] original, int depth) throws IOException {
        if (depth > 8) throw new IOException("Unexpected nested archive depth");
        var files = entries(original);
        if (depth == 0) {
            var manifest = required(files, "META-INF/toolvisuals/bundled-performance.json");
            var bundle = JsonParser.parseString(new String(manifest.bytes, StandardCharsets.UTF_8)).getAsJsonObject();
            for (var value : bundle.getAsJsonArray("mods")) {
                var mod = value.getAsJsonObject();
                if (mod.get("preserve_archive").getAsBoolean()) preserved.add(mod.get("id").getAsString());
            }
        }
        if (files.keySet().stream().anyMatch(JarCompactor::signature)) return original;
        var metadata = metadata(files);
        if (metadata != null && preserved.contains(metadata.get("id").getAsString())) return original;
        int configurationsBefore=compatibilityConfigurations;
        boolean via=metadata!=null&&"viafabricplus".equals(metadata.get("id").getAsString());
        if(via&&!LunarChatCompatibility.reviewedVersion(metadata.get("version").getAsString()))throw new IOException("ViaFabricPlus compatibility requires a reviewed catalog version");
        boolean configureVia=via&&LunarChatCompatibility.VIA_VERSION.equals(LunarChatCompatibility.upstreamVersion(metadata.get("version").getAsString()));
        if(configureVia)expectedCompatibilityConfigurations++;
        if(metadata!=null&&java.util.Set.of("voicechat","appleskin","fabric-api").contains(metadata.get("id").getAsString()))return original;
        var duplicates = new HashSet<String>();
        if (metadata != null && metadata.has("jars")) {
            var remaining = new JsonArray();
            for (var value : metadata.getAsJsonArray("jars")) {
                var path = value.getAsJsonObject().get("file").getAsString();
                var child = metadata(entries(required(files, path).bytes));
                if (replaceable(child)) duplicates.add(path);
                else remaining.add(value);
            }
            if (!duplicates.isEmpty()) metadata.add("jars", remaining);
        }
        var content = new ArrayList<Entry>(files.size());
        for (var entry : files.values()) {
            if (duplicates.contains(entry.name)) { removed++; continue; }
            byte[] bytes = entry.bytes;
            if (entry.name.endsWith(".jar")) bytes = compact(bytes, depth + 1);
            else if(configureVia&&entry.name.equals(LunarChatCompatibility.VIA_CONFIG)) {
                bytes=ViaFabricPlusCompatibility.configure(bytes);
                compatibilityConfigurations++;
            }
            else if (entry.name.equals(METADATA) && !duplicates.isEmpty()) bytes = JSON.toJson(metadata).getBytes(StandardCharsets.UTF_8);
            content.add(new Entry(entry.name, bytes, entry.timestamp));
        }
        byte[] result = archive(content);
        return result.length < original.length||compatibilityConfigurations>configurationsBefore ? result : original;
    }

    private boolean replaceable(JsonObject metadata) throws IOException {
        if (metadata == null) return false;
        var replacement = provided.get(metadata.get("id").getAsString());
        if (replacement == null) return false;
        try {
            if (Version.parse(replacement).compareTo(Version.parse(metadata.get("version").getAsString())) < 0) throw new IOException("Fabric API is older than its bundled module: " + metadata.get("id"));
        } catch (VersionParsingException error) { throw new IOException("Invalid Fabric API module version", error); }
        return true;
    }

    private static Map<String, Entry> entries(byte[] bytes) throws IOException {
        var result = new LinkedHashMap<String, Entry>();
        try (var zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (result.put(entry.getName(), new Entry(entry.getName(), zip.readAllBytes(), entry.getTime())) != null) throw new IOException("Duplicate archive entry: " + entry.getName());
            }
        }
        if (result.isEmpty()) throw new IOException("Empty or invalid archive");
        return result;
    }

    private static Entry required(Map<String, Entry> entries, String path) throws IOException {
        var value = entries.get(path);
        if (value == null) throw new IOException("Missing declared nested JAR: " + path);
        return value;
    }

    private static JsonObject metadata(Map<String, Entry> entries) {
        var entry = entries.get(METADATA);
        return entry == null ? null : JsonParser.parseString(new String(entry.bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static boolean signature(String name) {
        var path = name.toUpperCase(Locale.ROOT);
        return path.startsWith("META-INF/") && (path.endsWith(".SF") || path.endsWith(".RSA") || path.endsWith(".DSA") || path.endsWith(".EC"));
    }

    private static byte[] archive(List<Entry> files) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(bytes)) {
            zip.setLevel(Deflater.BEST_COMPRESSION);
            for (var file : files) {
                var entry = new ZipEntry(file.name);
                if (file.timestamp >= 0) entry.setTime(file.timestamp);
                zip.putNextEntry(entry); zip.write(file.bytes); zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
