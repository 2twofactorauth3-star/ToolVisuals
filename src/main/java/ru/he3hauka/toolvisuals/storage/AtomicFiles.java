package ru.he3hauka.toolvisuals.storage;

import java.io.IOException;
import java.nio.file.*;

public final class AtomicFiles {
    private AtomicFiles(){}
    public static void write(Path path,byte[] bytes) throws IOException {
        Path target=path.toAbsolutePath().normalize();Files.createDirectories(target.getParent());
        if(Files.isSymbolicLink(target))throw new IOException("Target is a symbolic link");
        var temporary=Files.createTempFile(target.getParent(),".toolvisuals-",".tmp");
        try{
            Files.write(temporary,bytes);
            try{Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException error){Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temporary);}
    }
}
