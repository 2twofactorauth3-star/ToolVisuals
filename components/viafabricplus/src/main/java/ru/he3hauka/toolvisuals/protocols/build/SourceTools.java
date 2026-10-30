package ru.he3hauka.toolvisuals.protocols.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public final class SourceTools {
    private SourceTools() {}

    public static void main(String[] arguments) throws Exception {
        switch (arguments[0]) {
            case "remap" -> remap(Path.of(arguments[1]), Path.of(arguments[2]), Path.of(arguments[3]),
                    arguments[4], arguments[5], arguments[6].equals("mixins"), Arrays.stream(arguments).skip(7).map(Path::of).toArray(Path[]::new));
            case "widen" -> widen(Path.of(arguments[1]), Path.of(arguments[2]),
                    Arrays.stream(arguments).skip(3).map(Path::of).toList());
            default -> throw new IllegalArgumentException("Unknown source preparation operation");
        }
    }

    private static void remap(Path input, Path output, Path mappings, String from, String to, boolean mixins, Path[] classpath) throws Exception {
        var builder = TinyRemapper.newRemapper().withMappings(TinyUtils.createTinyMappingProvider(mappings, from, to))
                .threads(Math.min(4, Runtime.getRuntime().availableProcessors())).resolveMissing(true);
        if (mixins) builder.extension(new MixinExtension());
        var remapper = builder.build();
        try (var target = new ZipOutputStream(Files.newOutputStream(output))) {
            remapper.readClassPath(classpath);
            remapper.readInputs(input);
            remapper.apply((name, bytes) -> {
                synchronized (target) {
                    try {
                        target.putNextEntry(new ZipEntry(name + ".class"));
                        target.write(bytes);
                        target.closeEntry();
                    } catch (IOException error) { throw new java.io.UncheckedIOException(error); }
                }
            });
        } finally { remapper.finish(); }
    }

    private static void widen(Path input, Path output, List<Path> accessFiles) throws Exception {
        Map<String, List<String[]>> rules = new HashMap<>();
        for (var file : accessFiles) for (String line : Files.readAllLines(file)) {
            if (line.isBlank() || line.startsWith("accessWidener") || line.startsWith("classTweaker")) continue;
            String[] rule = line.strip().split("\\s+");
            if (rule.length < 3) throw new IOException("Invalid access rule in " + file);
            rules.computeIfAbsent(rule[2], key -> new ArrayList<>()).add(rule);
        }
        try (var source = new ZipFile(input.toFile()); var target = new ZipOutputStream(Files.newOutputStream(output))) {
            for (var entry : Collections.list(source.entries())) {
                if (!entry.getName().endsWith(".class")) continue;
                byte[] bytes = source.getInputStream(entry).readAllBytes();
                var type = new ClassNode(); new ClassReader(bytes).accept(type, 0);
                boolean changed = false;
                for (var nested : type.innerClasses) if (rules.containsKey(nested.name)) {
                    for (var rule : rules.get(nested.name)) if (rule[1].equals("class")) {
                        nested.access = access(nested.access, rule[0]); changed = true;
                    }
                }
                for (var rule : rules.getOrDefault(type.name, List.of())) {
                    switch (rule[1]) {
                        case "class" -> { type.access = access(type.access, rule[0]); changed = true; }
                        case "field" -> {
                            for (var field : type.fields) if (field.name.equals(rule[3]) && field.desc.equals(rule[4])) {
                                field.access = access(field.access, rule[0]); changed = true;
                            }
                        }
                        case "method" -> {
                            for (var method : type.methods) if (method.name.equals(rule[3]) && method.desc.equals(rule[4])) {
                                method.access = access(method.access, rule[0]); changed = true;
                            }
                        }
                        default -> throw new IOException("Unknown access rule: " + rule[1]);
                    }
                }
                if (changed) { var writer = new ClassWriter(0); type.accept(writer); bytes = writer.toByteArray(); }
                target.putNextEntry(new ZipEntry(entry.getName())); target.write(bytes); target.closeEntry();
            }
        }
    }

    private static int access(int flags, String action) {
        return switch (action.replace("transitive-", "")) {
            case "mutable" -> flags & ~Opcodes.ACC_FINAL;
            case "extendable" -> flags & ~(Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
            case "accessible" -> flags & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
            default -> throw new IllegalArgumentException("Unknown access action: " + action);
        };
    }
}
