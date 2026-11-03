package ru.he3hauka.toolvisuals.client.cape.image;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

public final class CapeImageIo {
    private static final ThreadPoolExecutor WORKER=new ThreadPoolExecutor(1,1,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(1),job->{var thread=new Thread(job,"ToolVisuals cape images");thread.setDaemon(true);return thread;});
    @FunctionalInterface public interface Read<T>{T run() throws Exception;}
    private CapeImageIo() {}
    public static <T> void submit(Read<T> work,Consumer<T> complete,Consumer<Throwable> failed) {
        var client=Minecraft.getInstance();
        try {
            WORKER.execute(()->{
                try{var result=work.run();client.execute(()->complete.accept(result));}
                catch(Exception|LinkageError error){ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors.report("Process cape image",error);client.execute(()->failed.accept(error));}
            });
        } catch(java.util.concurrent.RejectedExecutionException error){failed.accept(error);}
    }
}
