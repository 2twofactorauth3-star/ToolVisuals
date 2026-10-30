package ru.he3hauka.toolvisuals.client.compatibility.files;

import java.nio.file.Path;
import java.util.Locale;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

public final class UserFolders {
    private UserFolders() {}
    public static void open(Path path){var platform=net.minecraft.Util.getPlatform();platform.openPath(path);}
    public static Path downloads() {
        if(System.getProperty("os.name","").toLowerCase(Locale.ROOT).contains("windows")) {
            try{return Path.of(com.sun.jna.platform.win32.Shell32Util.getKnownFolderPath(com.sun.jna.platform.win32.KnownFolders.FOLDERID_Downloads));}
            catch(RuntimeException|LinkageError error){ClientErrors.report("Find Windows Downloads folder",error);}
        }
        String configured=System.getenv("XDG_DOWNLOAD_DIR");
        if(configured!=null&&!configured.isBlank())return Path.of(configured.replace("$HOME",System.getProperty("user.home")));
        return Path.of(System.getProperty("user.home"),"Downloads");
    }
}
