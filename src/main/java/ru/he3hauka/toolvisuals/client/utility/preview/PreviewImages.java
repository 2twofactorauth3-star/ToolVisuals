package ru.he3hauka.toolvisuals.client.utility.preview;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.client.storage.BoundedInputStream;

public final class PreviewImages implements AutoCloseable {
    @FunctionalInterface public interface Input {InputStream open() throws IOException;}
    private record Image(Identifier texture,int width,int height){}
    private static long sequence;
    private static final class Texture extends DynamicTexture {
        Texture(Identifier id,NativeImage pixels){super(()->id.toString(),pixels);sampler=RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);}
    }
    private final Map<String,Image> images=new LinkedHashMap<>();
    private final Set<String> requested=new LinkedHashSet<>();
    private final ru.he3hauka.toolvisuals.client.utility.preview.LatestWorkQueue<String,Input> waiting=new ru.he3hauka.toolvisuals.client.utility.preview.LatestWorkQueue<>(16);
    private String active;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{var thread=new Thread(r,"ToolVisuals previews");thread.setDaemon(true);return thread;});
    private boolean closed;
    private final int maximum,cacheLimit;
    public PreviewImages(){this(512,32);}
    public PreviewImages(int maximum,int cacheLimit){
        if(maximum<1||maximum>2048||cacheLimit<1||cacheLimit>32)throw new IllegalArgumentException("Invalid preview cache limits");
        this.maximum=maximum;this.cacheLimit=cacheLimit;
    }
    public void request(String key,Input input) {
        if(closed||images.containsKey(key)||requested.contains(key)||key.equals(active))return;
        waiting.offer(key,input);
        pump();
    }
    private void pump(){
        if(closed||active!=null)return;
        var next=waiting.poll();if(next==null)return;
        active=next.getKey();
        try{
            worker.execute(()->{
                BufferedImage decoded;
                try{decoded=read(next.getValue(),maximum);}catch(Exception error){ClientErrors.report("Decode image preview",error);decoded=null;}
                var result=decoded;
                Minecraft.getInstance().execute(()->{
                    if(closed){if(result!=null)result.flush();active=null;return;}
                    if(result==null){markRequested(next.getKey());finish(next.getKey());return;}
                    boolean accepted=ru.he3hauka.toolvisuals.client.optimization.texture.TextureUploads.offer(this,(long)result.getWidth()*result.getHeight()*4,true,()->{
                        try{if(!closed){upload(next.getKey(),result);markRequested(next.getKey());}}
                        finally{result.flush();finish(next.getKey());}
                    },()->{result.flush();finish(next.getKey());});
                    if(!accepted){result.flush();finish(next.getKey());}
                });
            });
        }catch(RejectedExecutionException error){if(!closed)ClientErrors.report("Schedule image preview",error);active=null;}
    }
    private void markRequested(String key){requested.add(key);if(requested.size()>256)requested.remove(requested.iterator().next());}
    private void finish(String key){if(key.equals(active)){active=null;pump();}}
    public static BufferedImage read(Input input,int maximum) throws IOException {
        if(maximum<1||maximum>16384)throw new IllegalArgumentException("Invalid preview resolution");
        try(var stream=input.open()) {
            if(stream==null)return null;
            try(var imageStream=new MemoryCacheImageInputStream(new BoundedInputStream(stream,16*1024*1024))) {
                var readers=ImageIO.getImageReaders(imageStream);if(!readers.hasNext())return null;var reader=readers.next();
                try {
                    String format=reader.getFormatName();if(!format.equalsIgnoreCase("png")&&!format.equalsIgnoreCase("jpeg"))throw new IOException("Unsupported preview image format");
                    reader.setInput(imageStream,true,true);int width=reader.getWidth(0),height=reader.getHeight(0);
                    long area=(long)width*height;
                    if(width<=0||height<=0||width>32768||height>32768||area>64_000_000)throw new IOException("Image dimensions exceed the preview limit");
                    var params=reader.getDefaultReadParam();int sample=Math.max(Math.ceilDiv(Math.max(width,height),maximum),(int)Math.ceil(Math.sqrt(area/16_000_000d)));params.setSourceSubsampling(sample,sample,0,0);return reader.read(0,params);
                }finally{reader.dispose();}
            }
        }
    }
    private void upload(String key,BufferedImage image) {
        if(closed||image==null)return;
        var id=Identifier.fromNamespaceAndPath("toolvisuals","preview/"+sequence++);var pixels=new NativeImage(image.getWidth(),image.getHeight(),false);
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)pixels.setPixel(x,y,image.getRGB(x,y));
        var texture=new Texture(id,pixels);
        Minecraft.getInstance().getTextureManager().register(id,texture);images.put(key,new Image(id,image.getWidth(),image.getHeight()));
        if(images.size()>cacheLimit){var oldest=images.keySet().iterator().next();var removed=images.remove(oldest);requested.remove(oldest);Minecraft.getInstance().getTextureManager().release(removed.texture);}
    }
    public void draw(String key,GuiGraphicsExtractor g,float x,float y,float width,float height) {
        var image=images.get(key);if(image==null)return;float factor=Math.min(width/image.width,height/image.height),w=image.width*factor,h=image.height*factor;
        g.pose().pushMatrix();g.pose().translate(x+(width-w)/2,y+(height-h)/2);g.pose().scale(factor,factor);
        g.blit(RenderPipelines.GUI_TEXTURED,image.texture,0,0,0,0,image.width,image.height,image.width,image.height);g.pose().popMatrix();
    }
    @Override public void close(){closed=true;waiting.clear();ru.he3hauka.toolvisuals.client.optimization.texture.TextureUploads.cancel(this);worker.shutdownNow();images.values().forEach(image->Minecraft.getInstance().getTextureManager().release(image.texture));images.clear();requested.clear();}
}
