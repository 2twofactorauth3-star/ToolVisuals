package ru.he3hauka.toolvisuals.build;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipFile;

public final class MixinPackages {
    private MixinPackages() {}

    public static void main(String[] arguments) throws IOException {
        if (arguments.length != 1) throw new IllegalArgumentException("Expected a client JAR");
        verify(Path.of(arguments[0]));
        System.out.println("MIXIN_PACKAGES_OK isolated mixin namespaces and all registered classes present");
    }

    public static void verify(Path path) throws IOException {
        try (var archive = new ZipFile(path.toFile())) {
            var metadata = json(archive, "fabric.mod.json");
            var packages = new LinkedHashMap<String, Set<String>>();
            var registered = new LinkedHashSet<String>();
            for (var entry : metadata.getAsJsonArray("mixins")) {
                String resource = entry.isJsonPrimitive() ? entry.getAsString() : entry.getAsJsonObject().get("config").getAsString();
                var config = json(archive, resource);
                String prefix = config.get("package").getAsString().replace('.', '/') + '/';
                packages.computeIfAbsent(prefix, unused -> new LinkedHashSet<>()).add(resource);
                for (String side : new String[]{"mixins", "client", "server"}) {
                    if (!config.has(side)) continue;
                    for (var mixin : config.getAsJsonArray(side)) {
                        String name = prefix + mixin.getAsString().replace('.', '/');
                        if (archive.getEntry(name + ".class") == null) throw new IOException("Missing mixin class: " + name + " in " + resource);
                        registered.add(name);
                    }
                }
            }
            for (var entry : archive.stream().filter(item -> item.getName().endsWith(".class")).toList()) {
                String name = entry.getName().substring(0, entry.getName().length() - 6);
                int nested = name.indexOf('$');
                String owner = nested < 0 ? name : name.substring(0, nested);
                for (var namespace : packages.entrySet()) {
                    if (name.startsWith(namespace.getKey()) && !registered.contains(owner))
                        throw new IOException("Ordinary class inside reserved mixin package: " + name + " owned by " + namespace.getValue());
                }
            }
        }
    }

    private static JsonObject json(ZipFile archive, String name) throws IOException {
        var entry = archive.getEntry(name);
        if (entry == null) throw new IOException("Missing mixin metadata: " + name);
        try (var reader = new InputStreamReader(archive.getInputStream(entry), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
