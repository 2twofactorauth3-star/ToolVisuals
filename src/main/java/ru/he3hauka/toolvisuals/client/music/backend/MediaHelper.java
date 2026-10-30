package ru.he3hauka.toolvisuals.client.music.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import ru.he3hauka.toolvisuals.client.storage.BoundedJson;
import ru.he3hauka.toolvisuals.storage.AtomicFiles;

public final class MediaHelper {
    private static final String ROOT="/META-INF/toolvisuals/music/";
    private MediaHelper() {}
    public static boolean supported(String operatingSystem, String version) {
        if (!operatingSystem.toLowerCase(Locale.ROOT).startsWith("windows")) return false;
        try { return Integer.parseInt(version.split("\\.",2)[0]) >= 10; }
        catch (NumberFormatException error) { return false; }
    }
    public static Path extract(Path configDirectory) throws IOException {
        byte[] manifest=resource("music-helper.json",4096);
        var json=BoundedJson.parse(new String(manifest,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        if (json.get("protocol").getAsInt()!=MediaProtocol.VERSION || !json.get("file").getAsString().equals("ToolVisualsMedia.exe"))
            throw new IOException("Invalid bundled media helper manifest");
        byte[] bytes=resource("ToolVisualsMedia.exe",128*1024);
        String hash=hash(bytes);
        if (bytes.length!=json.get("bytes").getAsInt() || !hash.equals(json.get("sha256").getAsString()) || bytes.length<1024 || bytes[0]!='M' || bytes[1]!='Z')
            throw new IOException("Bundled media helper checksum mismatch");
        Path directory=configDirectory.toAbsolutePath().normalize().resolve("toolvisuals-cache").resolve("media");
        Files.createDirectories(directory);
        for (Path part=directory; part!=null && part.startsWith(configDirectory.toAbsolutePath().normalize()); part=part.getParent())
            if (Files.isSymbolicLink(part)) throw new IOException("Media helper directory must not be a symbolic link");
        Path file=directory.resolve("ToolVisualsMedia-"+hash.substring(0,16)+".exe");
        if (Files.exists(file,LinkOption.NOFOLLOW_LINKS)) {
            if (!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS) || Files.size(file)!=bytes.length || !hash(Files.readAllBytes(file)).equals(hash))
                throw new IOException("Cached media helper checksum mismatch; remove the modified cache file");
        } else AtomicFiles.write(file,bytes);
        return file;
    }
    private static byte[] resource(String name,int maximum) throws IOException {
        try (var stream=MediaHelper.class.getResourceAsStream(ROOT+name)) {
            if (stream==null) throw new IOException("Missing bundled media helper "+name);
            byte[] data=stream.readNBytes(maximum+1);
            if (data.length>maximum) throw new IOException("Bundled media helper exceeds its size limit");
            return data;
        }
    }
    private static String hash(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
}
