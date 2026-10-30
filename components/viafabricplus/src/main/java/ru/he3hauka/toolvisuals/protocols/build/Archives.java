package ru.he3hauka.toolvisuals.protocols.build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class Archives {
    private Archives() {}

    static Map<String, byte[]> read(byte[] bytes) throws IOException {
        var files = new LinkedHashMap<String, byte[]>();
        try (var zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory() && files.put(entry.getName(), zip.readAllBytes()) != null)
                    throw new IOException("Duplicate component entry: " + entry.getName());
            }
        }
        if (files.isEmpty()) throw new IOException("Empty component archive");
        return files;
    }

    static byte[] write(Map<String, byte[]> files) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(bytes)) {
            zip.setLevel(Deflater.BEST_COMPRESSION);
            for (var entry : files.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                var target = new ZipEntry(entry.getKey());
                target.setTimeLocal(java.time.LocalDateTime.of(1980, 1, 1, 0, 0));
                zip.putNextEntry(target);
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
