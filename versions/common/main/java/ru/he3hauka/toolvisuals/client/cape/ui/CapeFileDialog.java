package ru.he3hauka.toolvisuals.client.cape.ui;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

final class CapeFileDialog {
    private static final AtomicBoolean OPEN=new AtomicBoolean();
    private static Path lastDirectory;
    private CapeFileDialog() {}

    static void choose(boolean save,String title,Consumer<Path> chosen,Runnable failed) {
        var client=Minecraft.getInstance();
        if(!client.isSameThread()){client.execute(()->choose(save,title,chosen,failed));return;}
        if(!OPEN.compareAndSet(false,true))return;
        KeyMapping.releaseAll();
        Path directory=lastDirectory==null?client.gameDirectory.toPath():lastDirectory;
        Runnable dialog=()->{
            Path result=null;Throwable failure=null;
            try(var stack=MemoryStack.stackPush()) {
                var filters=stack.mallocPointer(1);filters.put(stack.UTF8("*.png")).flip();
                String selected=save?TinyFileDialogs.tinyfd_saveFileDialog(title,directory.resolve("ToolVisuals-Cape.png").toString(),filters,"PNG images")
                        :TinyFileDialogs.tinyfd_openFileDialog(title,directory.toString(),filters,"PNG images",false);
                if(selected!=null)result=CapeFilePaths.selected(selected,save);
            }catch(RuntimeException|LinkageError error){failure=error;}
            Path path=result;Throwable error=failure;
            client.execute(()->{
                OPEN.set(false);
                if(error!=null){ClientErrors.report("Open cape PNG dialog",error);failed.run();}
                else if(path!=null){lastDirectory=path.getParent();chosen.accept(path);}
            });
        };
        if(System.getProperty("os.name","").toLowerCase(java.util.Locale.ROOT).contains("mac"))dialog.run();
        else Thread.ofPlatform().name("ToolVisuals PNG dialog").daemon(true).start(ClientErrors.task("Cape PNG dialog",dialog));
    }
}
