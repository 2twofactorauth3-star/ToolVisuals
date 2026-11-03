package ru.he3hauka.toolvisuals.client.cape.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageReader;
import org.w3c.dom.Node;
import ru.he3hauka.toolvisuals.client.cape.CapeImageImport;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeAnimationPolicy;

final class GifCapeImport {
    static final int MAX_SOURCE_FRAMES=240;
    static final long MAX_DECODE_PIXELS=32L*1024*1024,MAX_DURATION_MILLIS=60_000;
    private record Frame(int x,int y,int width,int height,int delay,String disposal,boolean transparent) {}
    private GifCapeImport() {}

    static CapeImageImport read(ImageReader reader) throws IOException {
        int count=reader.getNumImages(true);
        if(count<1||count>MAX_SOURCE_FRAMES)throw new IOException("GIF frame count exceeds import limits");
        var stream=reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0");
        var screen=child(stream,"LogicalScreenDescriptor");
        int width=number(screen,"logicalScreenWidth"),height=number(screen,"logicalScreenHeight");
        CapeImages.dimensions(width,height);
        if((long)width*height*count>MAX_DECODE_PIXELS)throw new IOException("GIF decoding exceeds pixel work limit");
        var metadata=new ArrayList<Frame>(count);long duration=0;
        for(int i=0;i<count;i++) {
            var root=reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
            var bounds=child(root,"ImageDescriptor");var control=child(root,"GraphicControlExtension");
            int x=number(bounds,"imageLeftPosition"),y=number(bounds,"imageTopPosition"),w=number(bounds,"imageWidth"),h=number(bounds,"imageHeight");
            CapeImages.dimensions(w,h);
            if(x<0||y<0||(long)x+w>width||(long)y+h>height)throw new IOException("GIF frame outside its canvas");
            int delay=control==null?100:Math.max(10,number(control,"delayTime")*10);
            if(delay==10&&control!=null&&number(control,"delayTime")==0)delay=100;
            duration+=delay;if(duration>MAX_DURATION_MILLIS)throw new IOException("GIF duration exceeds import limit");
            metadata.add(new Frame(x,y,w,h,delay,control==null?"none":attribute(control,"disposalMethod"),control!=null&&"TRUE".equals(attribute(control,"transparentColorFlag"))));
        }
        boolean uniform=metadata.stream().allMatch(frame->frame.delay==metadata.getFirst().delay);
        int samples=count==1?1:uniform?Math.min(count,CapeAnimationPolicy.MAX_FRAMES):Math.min(CapeAnimationPolicy.MAX_FRAMES,Math.max(2,(int)Math.ceil(duration*CapeAnimationPolicy.MAX_FPS/1000d)));
        int fps=count==1?8:Math.clamp((int)Math.round(samples*1000d/duration),1,CapeAnimationPolicy.MAX_FPS);
        boolean adjusted=count!=samples||count>1&&Math.abs(duration-samples*1000d/fps)>10;
        var result=new ArrayList<CapeImageImport.Frame>(samples);
        var canvas=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        int background=background(stream);int sample=0;long start=0;
        try {
            fill(canvas,0,0,width,height,metadata.getFirst().transparent?0:background);
            for(int i=0;i<count&&sample<samples;i++) {
                var frame=metadata.get(i);BufferedImage previous=null;
                if(frame.disposal.equals("restoreToPrevious"))previous=copy(canvas);
                try {
                    var image=reader.read(i);
                    try {
                        if(image.getWidth()!=frame.width||image.getHeight()!=frame.height)throw new IOException("GIF decoded frame dimensions changed");
                        var graphics=canvas.createGraphics();
                        try{graphics.setComposite(AlphaComposite.SrcOver);graphics.drawImage(image,frame.x,frame.y,null);}
                        finally{graphics.dispose();}
                    } finally {image.flush();}
                    while(sample<samples&&(long)sample*duration/samples<start+frame.delay){result.add(new CapeImageImport.Frame(canvas));sample++;}
                    start+=frame.delay;
                    if(frame.disposal.equals("restoreToBackgroundColor"))fill(canvas,frame.x,frame.y,frame.width,frame.height,frame.transparent?0:background);
                    else if(previous!=null){var graphics=canvas.createGraphics();try{graphics.setComposite(AlphaComposite.Src);graphics.drawImage(previous,0,0,null);}finally{graphics.dispose();}}
                } finally {if(previous!=null)previous.flush();}
            }
        } finally {canvas.flush();}
        if(result.size()!=samples)throw new IOException("Incomplete GIF animation");
        return new CapeImageImport(width,height,result,fps,count,adjusted);
    }

    private static Node child(Node parent,String name) {
        if(parent!=null)for(var node=parent.getFirstChild();node!=null;node=node.getNextSibling())if(name.equals(node.getNodeName()))return node;
        return null;
    }
    private static String attribute(Node node,String name) throws IOException {
        var value=node==null?null:node.getAttributes().getNamedItem(name);
        if(value==null)throw new IOException("Missing GIF metadata: "+name);return value.getNodeValue();
    }
    private static int number(Node node,String name) throws IOException {
        try{return Integer.parseInt(attribute(node,name));}catch(NumberFormatException error){throw new IOException("Invalid GIF metadata",error);}
    }
    private static int background(Node stream) throws IOException {
        var table=child(stream,"GlobalColorTable");if(table==null)return 0;
        int index=number(table,"backgroundColorIndex");
        for(var color=table.getFirstChild();color!=null;color=color.getNextSibling())if("ColorTableEntry".equals(color.getNodeName())&&number(color,"index")==index)return 0xff000000|number(color,"red")<<16|number(color,"green")<<8|number(color,"blue");
        return 0;
    }
    private static BufferedImage copy(BufferedImage source) {
        var result=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
        var graphics=result.createGraphics();try{graphics.setComposite(AlphaComposite.Src);graphics.drawImage(source,0,0,null);}finally{graphics.dispose();}return result;
    }
    private static void fill(BufferedImage canvas,int x,int y,int width,int height,int color) {
        var graphics=canvas.createGraphics();try{graphics.setComposite(AlphaComposite.Src);graphics.setColor(new Color(color,true));graphics.fillRect(x,y,width,height);}finally{graphics.dispose();}
    }
}
