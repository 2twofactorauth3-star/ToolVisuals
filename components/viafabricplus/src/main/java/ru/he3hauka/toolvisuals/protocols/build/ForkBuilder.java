package ru.he3hauka.toolvisuals.protocols.build;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public final class ForkBuilder {
    private static final String METADATA = "fabric.mod.json";
    private static final List<String> BEDROCK_LIBRARIES = List.of("ViaBedrock-", "webrtc-java-", "netty-transport-nethernet-", "netty-transport-raknet-", "CubeConverter-", "mocha-", "forms-", "text-", "registry-5-", "dialog-5-");
    private static final String VIA = "com/viaversion/viafabricplus/";
    private static final String LOADER = "com/viaversion/vialoader/";
    private final Set<String> removedTypes = new HashSet<>();
    private final Set<String> retainedTypes = new HashSet<>();
    private final List<String> removedLibraries = new ArrayList<>();
    private final ProtocolPatches protocols;
    private final BedrockPatches bedrock = new BedrockPatches();
    private int changedClasses;

    private ForkBuilder(String minecraft) { protocols = new ProtocolPatches(minecraft); }

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 4) throw new IllegalArgumentException("Expected upstream JAR, component directory, Minecraft version and output JAR");
        var input = Path.of(arguments[0]);
        var component = Path.of(arguments[1]);
        String minecraft = arguments[2];
        var output = Path.of(arguments[3]);
        var manifest = JsonParser.parseString(Files.readString(component.resolve("upstream.json"))).getAsJsonObject();
        var profile = manifest.getAsJsonObject("targets").getAsJsonObject(minecraft);
        String minimum = manifest.get("minimum_protocol").getAsString();
        if (profile == null) throw new IOException("Unknown protocol component target: " + minecraft);
        byte[] original = Files.readAllBytes(input);
        var upstream = profile.getAsJsonObject("binary");
        if (original.length != upstream.get("bytes").getAsInt() || !sha512(original).equals(upstream.get("sha512").getAsString()))
            throw new IOException("Upstream ViaFabricPlus does not match its pinned checksum");
        var sources = new ArrayList<JsonObject>();
        sources.add(profile.getAsJsonObject("sources"));
        if (profile.has("related_sources")) for (var source : profile.getAsJsonArray("related_sources")) sources.add(source.getAsJsonObject());
        for (var source : sources) {
            byte[] bytes = Files.readAllBytes(input.resolveSibling(source.get("file").getAsString()));
            if (bytes.length != source.get("bytes").getAsInt() || !sha512(bytes).equals(source.get("sha512").getAsString()))
                throw new IOException("Upstream corresponding sources do not match their pinned checksum");
        }
        var builder = new ForkBuilder(minecraft);
        builder.discover(original, false);
        byte[] result = builder.transform(original, true);
        var files = Archives.read(result);
        var metadata = metadata(files);
        if (!metadata.get("version").getAsString().equals(profile.get("version").getAsString())) throw new IOException("Upstream version differs from the fork profile");
        metadata.addProperty("name", "ToolVisuals Protocols (ViaFabricPlus)");
        metadata.addProperty("version", profile.get("version").getAsString() + "+toolvisuals." + manifest.get("revision").getAsInt());
        metadata.addProperty("description", "ToolVisuals Java protocol component: Minecraft " + minimum + " through " + minecraft + ". Based on ViaFabricPlus; Bedrock transports are removed.");
        metadata.getAsJsonObject("depends").addProperty("toolvisuals", ">=1.4");
        var provenance = new JsonObject();
        provenance.addProperty("schema", 1);
        provenance.addProperty("revision", manifest.get("revision").getAsInt());
        provenance.addProperty("patch_sources_sha256", sourceDigest(component));
        provenance.addProperty("minecraft", minecraft);
        provenance.addProperty("minimum_java_protocol", minimum);
        provenance.addProperty("retired_account_stores", builder.bedrock.accounts);
        provenance.add("upstream", profile.deepCopy());
        provenance.addProperty("changed_classes", builder.changedClasses);
        provenance.addProperty("removed_classes", builder.removedTypes.size());
        var libraries = new JsonArray();
        builder.removedLibraries.stream().sorted().forEach(libraries::add);
        provenance.add("removed_libraries", libraries);
        metadata.getAsJsonObject("custom").add("toolvisuals:protocol-fork", provenance.deepCopy());
        files.put(METADATA, json(metadata));
        files.put("META-INF/toolvisuals/protocol-component.json", json(provenance));
        files.put("META-INF/toolvisuals/protocols-NOTICE", Files.readString(component.resolve("NOTICE")).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8));
        files.put("META-INF/toolvisuals/protocols-COPYING", Files.readString(component.resolve("COPYING")).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8));
        result = Archives.write(files);
        builder.validate(result);
        if (builder.protocols.guards < 1 || builder.protocols.routes < 1)
            throw new IOException("Protocol component did not apply its expected integration patches");
        if (!minecraft.equals("26.3") && (builder.removedLibraries.isEmpty() || builder.bedrock.platforms < 1
                || builder.bedrock.accounts != 1 || builder.bedrock.arrays < 3 || builder.bedrock.providers < 1))
            throw new IOException("Expected Bedrock platform removal was not applied");
        output.toAbsolutePath().getParent().toFile().mkdirs();
        if (!Files.isRegularFile(output) || !java.util.Arrays.equals(Files.readAllBytes(output), result)) {
            var temporary = output.resolveSibling(output.getFileName() + ".tmp");
            Files.write(temporary, result);
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.printf(java.util.Locale.ROOT, "PROTOCOL_FORK_OK minecraft=%s before=%d after=%d removed_libraries=%d removed_classes=%d guards=%d catalogs=%d routes=%d sha512=%s%n",
                minecraft, original.length, result.length, builder.removedLibraries.size(), builder.removedTypes.size(), builder.protocols.guards, builder.protocols.catalogs, builder.protocols.routes, sha512(result));
    }

    private static String sha512(byte[] bytes) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-512").digest(bytes)); }
    private static String sourceDigest(Path component) throws Exception {
        var paths = new ArrayList<Path>();
        for (String name : List.of("upstream.json", "NOTICE", "COPYING")) paths.add(component.resolve(name));
        try (var sources = Files.walk(component.resolve("src/main/java"))) { paths.addAll(sources.filter(path -> path.toString().endsWith(".java")).toList()); }
        paths.sort(java.util.Comparator.comparing(path -> component.relativize(path).toString().replace('\\', '/')));
        var digest = MessageDigest.getInstance("SHA-256");
        for (var path : paths) {
            digest.update(component.relativize(path).toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(Files.readString(path).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static byte[] json(JsonObject object) { return new GsonBuilder().setPrettyPrinting().create().toJson(object).getBytes(StandardCharsets.UTF_8); }
    private static JsonObject metadata(Map<String, byte[]> files) { return JsonParser.parseString(new String(files.get(METADATA), StandardCharsets.UTF_8)).getAsJsonObject(); }
    private static boolean library(String name) {
        String file = name.substring(name.lastIndexOf('/') + 1);
        return BEDROCK_LIBRARIES.stream().anyMatch(file::startsWith);
    }

    private void discover(byte[] bytes, boolean remove) throws IOException {
        for (var entry : Archives.read(bytes).entrySet()) {
            if (entry.getKey().endsWith(".jar")) discover(entry.getValue(), remove || library(entry.getKey()));
            else if (entry.getKey().endsWith(".class")) {
                String name = new ClassReader(entry.getValue()).getClassName();
                if (remove || (name.startsWith(VIA) || name.startsWith(LOADER)) && BedrockPatches.removed(name)) removedTypes.add(name);
                else retainedTypes.add(name);
            }
        }
    }

    private byte[] transform(byte[] original, boolean root) throws Exception {
        var files = Archives.read(original);
        var metadata = metadata(files);
        boolean changed = root;
        var jars = new JsonArray();
        if (metadata.has("jars")) for (var item : metadata.getAsJsonArray("jars")) {
            String file = item.getAsJsonObject().get("file").getAsString();
            if (library(file)) {
                files.remove(file);
                removedLibraries.add(file);
                changed = true;
            } else jars.add(item);
        }
        if (metadata.has("jars")) metadata.add("jars", jars);
        for (var entry : List.copyOf(files.entrySet())) {
            String name = entry.getKey();
            if (name.endsWith(".jar")) {
                byte[] nested = transform(entry.getValue(), false);
                if (nested != entry.getValue()) { files.put(name, nested); changed = true; }
            } else if (name.endsWith(".class")) {
                var reader = new ClassReader(entry.getValue());
                String typeName = reader.getClassName();
                if (removedTypes.contains(typeName)) { files.remove(name); changed = true; continue; }
                if (!typeName.startsWith(VIA) && !typeName.startsWith(LOADER)) continue;
                var type = new ClassNode();
                reader.accept(type, ClassReader.EXPAND_FRAMES);
                boolean modified = type.innerClasses.removeIf(nested -> removedTypes.contains(nested.name));
                if (type.nestMembers != null) modified |= type.nestMembers.removeIf(removedTypes::contains);
                modified |= protocols.patch(type);
                modified |= bedrock.patch(type);
                if (!modified) continue;
                var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                type.accept(writer);
                files.put(name, writer.toByteArray());
                changedClasses++;
                changed = true;
            } else if (name.endsWith(".mixins.json")) {
                var config = JsonParser.parseString(new String(entry.getValue(), StandardCharsets.UTF_8)).getAsJsonObject();
                String prefix = config.get("package").getAsString().replace('.', '/') + '/';
                for (String side : List.of("client", "mixins", "server")) if (config.has(side)) {
                    var remaining = new JsonArray();
                    for (var item : config.getAsJsonArray(side)) if (!removedTypes.contains(prefix + item.getAsString().replace('.', '/'))) remaining.add(item);
                    config.add(side, remaining);
                }
                files.put(name, json(config));
                changed = true;
            }
        }
        if (changed) { files.put(METADATA, json(metadata)); return Archives.write(files); }
        return original;
    }

    private void validate(byte[] bytes) throws IOException {
        var failures = new ArrayList<String>();
        validate(bytes, failures);
        if (!failures.isEmpty()) throw new IOException("Protocol fork retains removed dependencies:\n" + String.join("\n", failures.stream().distinct().sorted().limit(80).toList()));
    }

    private void validate(byte[] bytes, List<String> failures) throws IOException {
        var files = Archives.read(bytes);
        var metadata = metadata(files);
        if (metadata.has("jars")) for (var jar : metadata.getAsJsonArray("jars"))
            if (!files.containsKey(jar.getAsJsonObject().get("file").getAsString())) throw new IOException("Missing declared component dependency");
        for (var entry : files.entrySet()) {
            if (entry.getKey().endsWith(".jar")) validate(entry.getValue(), failures);
            else if (entry.getKey().endsWith(".class")) {
                String owner = new ClassReader(entry.getValue()).getClassName();
                for (String target : ReferencedTypes.read(entry.getValue()))
                    if (removedTypes.contains(target) && !retainedTypes.contains(target)) failures.add(owner + " -> " + target);
            }
        }
    }
}
