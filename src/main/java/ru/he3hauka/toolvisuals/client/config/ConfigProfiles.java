package ru.he3hauka.toolvisuals.client.config;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ru.he3hauka.toolvisuals.client.storage.BoundedJson;

public final class ConfigProfiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final int LIMIT = 64, MAX_BYTES = BoundedJson.MAX_BYTES;
    public static final class Profile {
        public final String id, name, settings;
        public final long updatedAt;
        public final int enabledModules;
        Profile(String id, String name, long updatedAt, String settings) {
            this.id = id; this.name = name; this.updatedAt = updatedAt; this.settings = settings;
            var json = JsonParser.parseString(settings).getAsJsonObject();
            enabledModules = json.getAsJsonArray("modules").size() + (json.get("noRenderEnabled").getAsBoolean() ? 1 : 0);
        }
    }
    public final Path directory;
    private List<Profile> profiles = List.of();
    public ConfigProfiles(Path directory) { this.directory = directory.toAbsolutePath().normalize(); }
    public List<Profile> profiles() { return profiles; }
    public void refresh() throws IOException {
        Files.createDirectories(directory);
        var found = new ArrayList<Profile>();
        try (var files = Files.list(directory)) {
            for (var file : files.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().limit(256).toList()) {
                if (found.size() == LIMIT) break;
                try {
                    if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || Files.size(file) > MAX_BYTES) continue;
                    var data = BoundedJson.read(file).getAsJsonObject();
                    if (!data.has("version") || data.get("version").getAsInt() != 1) continue;
                    String id = data.get("id").getAsString();
                    if (!path(id).equals(file.toAbsolutePath().normalize())) continue;
                    String name = validName(data.get("name").getAsString());
                    String settings = ClientConfig.normalizePreset(GSON.toJson(data.getAsJsonObject("settings")));
                    found.add(new Profile(id, name, Math.max(0, data.get("updatedAt").getAsLong()), settings));
                } catch (IOException | com.google.gson.JsonParseException | IllegalArgumentException | IllegalStateException | NullPointerException error) {
                    ClientErrors.report("Read saved config profile "+file.getFileName(),error);
                }
            }
        }
        profiles = found.stream().sorted(Comparator.comparingLong((Profile p) -> p.updatedAt).reversed()
                .thenComparing(p -> p.name, String.CASE_INSENSITIVE_ORDER)).toList();
    }
    public boolean nameAvailable(String name, String except) {
        try { String valid = validName(name); return profiles.stream().noneMatch(p -> !p.id.equals(except) && p.name.equalsIgnoreCase(valid)); }
        catch (IllegalArgumentException error) { return false; }
    }
    public Profile create(String name, ClientConfig config) throws IOException { return create(name, config.presetJson()); }
    private Profile create(String name, String settings) throws IOException {
        if (profiles.size() >= LIMIT) throw new IllegalStateException("Profile limit reached");
        name = validName(name);
        if (!nameAvailable(name, "")) throw new IllegalArgumentException("Duplicate profile name");
        var profile = new Profile(UUID.randomUUID().toString(), name, System.currentTimeMillis(), ClientConfig.normalizePreset(settings));
        write(profile); refresh(); return profile;
    }
    public Profile overwrite(String id, ClientConfig config) throws IOException {
        var old = require(id); var profile = new Profile(id, old.name, System.currentTimeMillis(), config.presetJson());
        write(profile); refresh(); return profile;
    }
    public Profile rename(String id, String name) throws IOException {
        var old = require(id); name = validName(name);
        if (!nameAvailable(name, id)) throw new IllegalArgumentException("Duplicate profile name");
        var profile = new Profile(id, name, System.currentTimeMillis(), old.settings); write(profile); refresh(); return profile;
    }
    public void delete(String id) throws IOException { require(id); Files.delete(path(id)); refresh(); }
    public void apply(String id, ClientConfig config) { config.applyPreset(require(id).settings); }
    public String exportJson(String id) {
        var profile = require(id); var data = new JsonObject();
        data.addProperty("format", "ToolVisuals"); data.addProperty("version", 1); data.addProperty("name", profile.name);
        data.add("settings", JsonParser.parseString(profile.settings)); return GSON.toJson(data);
    }
     
    public Path download(String id, Path downloads) throws IOException {
        var profile = require(id);
        String json = exportJson(id);
        String stem = profile.name.replaceAll("[\\p{Cntrl}<>:\"/\\\\|?*]", "_").replaceAll("[. ]+$", "");
        if (stem.isBlank()) stem = "ToolVisuals";
         
        if (stem.split("\\.", 2)[0].matches("(?i)CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]")) stem = "ToolVisuals-" + stem;
        Path folder = downloads.toAbsolutePath().normalize();
        Files.createDirectories(folder);
        for (int suffix = 0; suffix < 10_000; suffix++) {
            Path file = folder.resolve(stem + (suffix == 0 ? "" : " (" + suffix + ")") + ".json");
            try {
                Files.writeString(file, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                return file;
            } catch (FileAlreadyExistsException ignored) { }
        }
        throw new IOException("Too many downloads with the same name");
    }
    public Profile importJson(String json, String fallbackName) throws IOException {
        if (json == null || json.length() > MAX_BYTES) throw new IllegalArgumentException("Invalid preset size");
        var data = BoundedJson.parse(json).getAsJsonObject();
        String name = fallbackName; String settings = json;
        if (data.has("settings")) {
            if (!data.has("format") || !data.get("format").getAsString().equals("ToolVisuals")
                    || !data.has("version") || data.get("version").getAsInt() != 1) throw new IllegalArgumentException("Unknown preset format");
            settings = GSON.toJson(data.getAsJsonObject("settings"));
            if (data.has("name")) name = data.get("name").getAsString();
        }
        name = validName(name); String unique = name;
        for (int suffix = 2; !nameAvailable(unique, ""); suffix++) unique = name.substring(0, name.offsetByCodePoints(0, Math.min(name.codePointCount(0, name.length()), 40))) + " (" + suffix + ")";
        return create(unique, ClientConfig.normalizePreset(settings));
    }
    private Profile require(String id) { return profiles.stream().filter(p -> p.id.equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown preset")); }
    private Path path(String id) {
        if (id == null || !UUID.fromString(id).toString().equals(id)) throw new IllegalArgumentException("Invalid preset id");
        Path path = directory.resolve(id + ".json").normalize();
        if (!path.startsWith(directory)) throw new IllegalArgumentException("Invalid preset path");
        return path;
    }
    private void write(Profile profile) throws IOException {
        Path target = path(profile.id);
        var data = new JsonObject(); data.addProperty("version", 1); data.addProperty("id", profile.id);
        data.addProperty("name", profile.name); data.addProperty("updatedAt", profile.updatedAt);
        data.add("settings", JsonParser.parseString(profile.settings));
        ru.he3hauka.toolvisuals.client.storage.JsonFile.write(target,GSON.toJson(data),MAX_BYTES);
    }
    public static String validName(String name) {
        if (name == null) throw new IllegalArgumentException("Missing profile name");
        name = name.strip();
        if (name.isEmpty() || name.codePointCount(0,name.length()) > 48 || name.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid profile name");
        return name;
    }
}
