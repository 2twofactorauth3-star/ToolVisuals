package ru.he3hauka.toolvisuals.client.cape.ui;

import java.nio.file.Path;
import java.util.Locale;

final class CapeFilePaths {
    private CapeFilePaths() {}
    static Path selected(String filename, boolean save) {
        return selected(filename,save,false);
    }
    static Path selected(String filename, boolean save, boolean animated) {
        if (filename == null || filename.isEmpty()) return null;
        var path = Path.of(filename).toAbsolutePath().normalize();
        if (path.getFileName() == null) throw new IllegalArgumentException("Expected a cape file");
        String name=path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (save && !name.endsWith(".png")&&!name.endsWith(".gif"))
            path = path.resolveSibling(path.getFileName() + (animated?".gif":".png"));
        return path;
    }
}
