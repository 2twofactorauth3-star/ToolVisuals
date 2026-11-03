package ru.he3hauka.toolvisuals.client.gui.dialog;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;
import ru.he3hauka.toolvisuals.client.compatibility.input.NativeEventThread;

import static org.lwjgl.sdl.SDLDialog.*;
import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLProperties.*;

public final class FileDialogs {
    private static final AtomicBoolean OPEN=new AtomicBoolean();
    private static final SDL_DialogFileCallback CALLBACK=SDL_DialogFileCallback.create((userdata,files,filter)->complete(files));
    private static volatile Request pending;
    private static Path lastDirectory;
    private static final class Request implements AutoCloseable {
        final Minecraft client;
        final boolean save;
        final String description,extensions,defaultName;
        final Consumer<Path> chosen;
        final Runnable failed;
        ByteBuffer name,pattern;
        SDL_DialogFileFilter.Buffer filters;
        int properties;
        Request(Minecraft client,boolean save,String description,String extensions,String defaultName,Consumer<Path> chosen,Runnable failed){this.client=client;this.save=save;this.description=description;this.extensions=extensions;this.defaultName=defaultName;this.chosen=chosen;this.failed=failed;}
        void open(String title){
            name=MemoryUtil.memUTF8(description);pattern=MemoryUtil.memUTF8(extensions);
            filters=SDL_DialogFileFilter.calloc(1).name(name).pattern(pattern);
            properties=SDL_CreateProperties();
            if(properties==0||!SDL_SetPointerProperty(properties,SDL_PROP_FILE_DIALOG_FILTERS_POINTER,filters.address())
                    ||!SDL_SetNumberProperty(properties,SDL_PROP_FILE_DIALOG_NFILTERS_NUMBER,1)
                    ||!SDL_SetPointerProperty(properties,SDL_PROP_FILE_DIALOG_WINDOW_POINTER,client.getWindow().handle())
                    ||!SDL_SetStringProperty(properties,SDL_PROP_FILE_DIALOG_TITLE_STRING,title)
                    ||!SDL_SetBooleanProperty(properties,SDL_PROP_FILE_DIALOG_MANY_BOOLEAN,false))
                throw new IllegalStateException("Cannot configure file dialog: "+SDL_GetError());
            var directory=lastDirectory==null?client.gameDirectory.toPath():lastDirectory;
            var location=save?directory.resolve(defaultName):directory;
            if(!SDL_SetStringProperty(properties,SDL_PROP_FILE_DIALOG_LOCATION_STRING,location.toAbsolutePath().toString()))
                throw new IllegalStateException("Cannot set file dialog location: "+SDL_GetError());
            SDL_ShowFileDialogWithProperties(save?SDL_FILEDIALOG_SAVEFILE:SDL_FILEDIALOG_OPENFILE,CALLBACK,0,properties);
        }
        @Override public void close(){
            if(properties!=0){SDL_DestroyProperties(properties);properties=0;}
            if(filters!=null){filters.free();filters=null;}
            MemoryUtil.memFree(name);MemoryUtil.memFree(pattern);name=pattern=null;
        }
    }
    private FileDialogs(){}
    public static void choose(boolean save,String title,String description,String extensions,String defaultName,Consumer<Path> chosen,Runnable failed){
        var client=Minecraft.getInstance();
        if(!client.isSameThread()){client.execute(()->choose(save,title,description,extensions,defaultName,chosen,failed));return;}
        if(!OPEN.compareAndSet(false,true))return;
        net.minecraft.client.KeyMapping.releaseAll();
        var request=new Request(client,save,description,extensions,defaultName,chosen,failed);pending=request;
        NativeEventThread.execute(()->{
            try{request.open(title);}
            catch(RuntimeException|LinkageError error){client.execute(()->finish(request,null,error));}
        });
    }
    private static void complete(long files){
        var request=pending;if(request==null)return;
        Path selected=null;Throwable failure=null;
        try{
            if(files==MemoryUtil.NULL)throw new IllegalStateException("file dialog failed: "+SDL_GetError());
            long filename=MemoryUtil.memGetAddress(files);
            if(filename!=MemoryUtil.NULL)selected=Path.of(MemoryUtil.memUTF8(filename)).toAbsolutePath().normalize();
        }catch(RuntimeException error){failure=error;}
        Path result=selected;Throwable error=failure;
        request.client.execute(()->finish(request,result,error));
    }
    private static void finish(Request request,Path selected,Throwable error){
        if(pending!=request)return;
        pending=null;
        try{request.close();}finally{OPEN.set(false);}
        if(error!=null){ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors.report("Open native file dialog",error);request.failed.run();}
        else if(selected!=null){lastDirectory=selected.getParent();request.chosen.accept(selected);}
    }
}
