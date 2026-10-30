package ru.he3hauka.toolvisuals.client.cape.ui;

import java.nio.file.Path;
import java.util.Locale;

final class CapeFilePaths {
    private CapeFilePaths() {}
    static Path selected(String filename, boolean save) {
        if (filename == null || filename.isEmpty()) return null;
        var path = Path.of(filename).toAbsolutePath().normalize();
        if (path.getFileName() == null) throw new IllegalArgumentException("Expected a cape file");
        if (save && !path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
            path = path.resolveSibling(path.getFileName() + ".png");
        return path;
    }
}
