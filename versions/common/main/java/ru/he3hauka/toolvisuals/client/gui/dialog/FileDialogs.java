package ru.he3hauka.toolvisuals.client.gui.dialog;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

public final class FileDialogs {
    private static final AtomicBoolean OPEN=new AtomicBoolean();
    private static Path lastDirectory;
    private FileDialogs() {}

    public static void choose(boolean save,String title,String description,String extensions,String defaultName,Consumer<Path> chosen,Runnable failed) {
        var client=Minecraft.getInstance();
        if(!client.isSameThread()){client.execute(()->choose(save,title,description,extensions,defaultName,chosen,failed));return;}
        if(!OPEN.compareAndSet(false,true))return;
        KeyMapping.releaseAll();
        Path directory=lastDirectory==null?client.gameDirectory.toPath():lastDirectory;
        Runnable dialog=()->{
            Path result=null;Throwable failure=null;
            try(var stack=MemoryStack.stackPush()) {
                String[] patterns=extensions.split(";");var filters=stack.mallocPointer(patterns.length);for(String pattern:patterns)filters.put(stack.UTF8("*."+pattern));filters.flip();
                String selected=save?TinyFileDialogs.tinyfd_saveFileDialog(title,directory.resolve(defaultName).toString(),filters,description)
                        :TinyFileDialogs.tinyfd_openFileDialog(title,directory.toString(),filters,description,false);
                if(selected!=null)result=Path.of(selected).toAbsolutePath().normalize();
            }catch(RuntimeException|LinkageError error){failure=error;}
            Path path=result;Throwable error=failure;
            client.execute(()->{
                OPEN.set(false);
                if(error!=null){ClientErrors.report("Open native file dialog",error);failed.run();}
                else if(path!=null){lastDirectory=path.getParent();chosen.accept(path);}
            });
        };
        if(System.getProperty("os.name","").toLowerCase(java.util.Locale.ROOT).contains("mac"))dialog.run();
        else Thread.ofPlatform().name("ToolVisuals file dialog").daemon(true).start(ClientErrors.task("Native file dialog",dialog));
    }
}
