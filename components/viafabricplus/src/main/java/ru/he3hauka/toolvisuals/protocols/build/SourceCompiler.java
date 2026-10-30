package ru.he3hauka.toolvisuals.protocols.build;

import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import javax.tools.ToolProvider;

public final class SourceCompiler {
    private SourceCompiler() {}

    public static void main(String[] arguments) throws Exception {
        var lines = Files.readAllLines(Path.of(arguments[0]), StandardCharsets.UTF_8);
        int separator = lines.indexOf("--SOURCES--");
        if (separator < 0) throw new IllegalArgumentException("Missing compiler source list");
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("A complete JDK is required");
        var manager = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);
        boolean success;
        try {
            success = compiler.getTask(null, manager, null, lines.subList(0, separator), null,
                    manager.getJavaFileObjectsFromStrings(lines.subList(separator + 1, lines.size()))).call();
        } finally {
            try { manager.close(); }
            catch (AccessDeniedException error) {
                if (Arrays.stream(error.getStackTrace()).noneMatch(frame -> frame.getClassName().equals("jdk.nio.zipfs.ZipFileSystemProvider")
                        && frame.getMethodName().equals("removeFileSystem"))) throw error;
                System.err.println("Compiler archive cleanup could not normalize a restricted cache path:");
                error.printStackTrace(System.err);
            }
        }
        if (!success) System.exit(1);
    }
}
