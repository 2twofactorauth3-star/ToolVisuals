package ru.he3hauka.toolvisuals.client.optimization.texture;

import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

public final class TextureUploads {
    private static final UploadQueue<Object> QUEUE=new UploadQueue<>(512);
    private TextureUploads(){}
    public static boolean offer(Object owner,long bytes,boolean foreground,Runnable upload,Runnable cancelled){
        return QUEUE.offer(owner,new UploadQueue.Job(bytes,foreground,()->{
            try{upload.run();}catch(RuntimeException error){org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot upload cosmetic texture",error);throw error;}
        },cancelled));
    }
    public static void cancel(Object owner){QUEUE.cancel(owner);}
    public static void drain(){
        if(QUEUE.size()==0)return;
        long allowance=ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork.textures(1_000_000L);
        if(allowance<=0)return;
        long started=Diagnostics.start(Diagnostics.Section.TEXTURE_UPLOAD),workStarted=System.nanoTime();
        int completed=0;
        try{completed=QUEUE.drain(2,256*1024,allowance,System::nanoTime);}finally{
            ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork.consumed(System.nanoTime()-workStarted,completed>0);
            Diagnostics.end(Diagnostics.Section.TEXTURE_UPLOAD,started);
        }
    }
    public static void clear(){QUEUE.clear();}
}
