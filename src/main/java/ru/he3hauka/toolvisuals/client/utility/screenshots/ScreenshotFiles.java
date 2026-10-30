package ru.he3hauka.toolvisuals.client.utility.screenshots;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class ScreenshotFiles {
    private ScreenshotFiles(){}
    public static List<Path> list(Path directory) throws IOException {
        if(!Files.isDirectory(directory))return List.of();
        try(var paths=Files.list(directory)){return paths.filter(path->Files.isRegularFile(path)&&path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")).sorted(Comparator.comparingLong(ScreenshotFiles::modified).reversed()).limit(2000).toList();}
    }
    private static long modified(Path path){try{return Files.getLastModifiedTime(path).toMillis();}catch(IOException error){ClientErrors.report("Read screenshot file metadata",error);return 0;}}
}
