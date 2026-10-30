package ru.he3hauka.toolvisuals.client.optimization.diagnostics;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public final class MarkerLog {
    public static final long MAX_BYTES=2*1024*1024;
    private final Path path;
    public MarkerLog(Path path){this.path=path;}
    private BasicFileAttributes attributes() throws IOException {return Files.readAttributes(path,BasicFileAttributes.class,LinkOption.NOFOLLOW_LINKS);}
    public boolean present(){
        try{return attributes().isRegularFile();}
        catch(NoSuchFileException error){return false;}
        catch(IOException|SecurityException error){ClientErrors.report("Read performance diagnostic marker",error);return false;}
    }
    public long size() throws IOException {
        try{var attributes=attributes();return attributes.isRegularFile()?attributes.size():-1;}
        catch(NoSuchFileException error){return -1;}
    }
    public boolean append(String text) throws IOException {
        var before=attributes();if(!before.isRegularFile())return false;
        byte[] bytes=text.getBytes(StandardCharsets.UTF_8);
        if(before.size()>MAX_BYTES-bytes.length)return false;
        try(var channel=FileChannel.open(path,StandardOpenOption.WRITE,StandardOpenOption.APPEND,LinkOption.NOFOLLOW_LINKS)){
            var current=attributes();
            if(!current.isRegularFile()||!Objects.equals(before.fileKey(),current.fileKey())||channel.size()>MAX_BYTES-bytes.length)return false;
            var buffer=ByteBuffer.wrap(bytes);while(buffer.hasRemaining())channel.write(buffer);return true;
        }
    }
}
