package ru.he3hauka.toolvisuals.client.cape;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import javax.imageio.ImageIO;

import ru.he3hauka.toolvisuals.storage.AtomicFiles;
import ru.he3hauka.toolvisuals.client.storage.BoundedInputStream;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

import static ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign.*;

public final class CapePng {
    public static final int MAX_BYTES=256*1024, ATLAS_WIDTH=CapeAtlas.WIDTH, ATLAS_HEIGHT=CapeAtlas.HEIGHT;
    public static final int MAX_IMPORT_BYTES=8*1024*1024,MAX_IMPORT_PIXELS=4*1024*1024,MAX_IMPORT_SIDE=4096;
    private static final long SIGNATURE=0x89504e470d0a1a0aL;
    private CapePng(){}
    public static CapeDesign read(Path path) throws IOException {
        try(var stream=new BoundedInputStream(Files.newInputStream(path),MAX_BYTES)){return decode(stream.readAllBytes());}
    }
    public static CapeImageImport readImport(Path path) throws IOException {
        return ru.he3hauka.toolvisuals.client.cape.image.CapeImages.read(path);
    }
    public static CapeDesign decode(byte[] bytes) throws IOException {
        return fromImage(image(bytes,true));
    }
    private static BufferedImage image(byte[] bytes,boolean exact) throws IOException {
        if(bytes==null||bytes.length<33||bytes.length>(exact?MAX_BYTES:MAX_IMPORT_BYTES))throw new IOException("PNG exceeds cape limits");
        var header=ByteBuffer.wrap(bytes);
        if(header.getLong()!=SIGNATURE||header.getInt()!=13||header.getInt()!=0x49484452)throw new IOException("Expected a PNG image");
        int width=header.getInt(),height=header.getInt();
        if(width<1||height<1||width>MAX_IMPORT_SIDE||height>MAX_IMPORT_SIDE||(long)width*height>MAX_IMPORT_PIXELS)
            throw new IOException("PNG dimensions exceed import limits");
        if(exact&&CapeImageImport.Layout.match(width,height)==null)throw new IOException("Use PNG 20x32, 40x32, 64x32 or 128x64");
        BufferedImage image=ImageIO.read(new ByteArrayInputStream(bytes));
        if(image==null||image.getWidth()!=width||image.getHeight()!=height)throw new IOException("Invalid cape PNG");
        return image;
    }
    static CapeDesign fromImage(BufferedImage image) {
        int width=image.getWidth();
        if(CapeImageImport.Layout.match(width,image.getHeight())==null)throw new IllegalArgumentException("Unsupported cape layout");
        int[] pixels=new int[PIXELS];
        for(var face:Face.values())for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){
            int sx=x,sy=y;
            if(width==40)sx+=(face==Face.BACK?WIDTH:0);
            else if(width==64){sx=(face==Face.FRONT?1:12)+x/2;sy=1+y/2;}
            else if(width==128){sx+=(face==Face.FRONT?2:24);sy+=2;}
            pixels[index(face,x,y)]=image.getRGB(sx,sy);
        }
        return new CapeDesign(pixels);
    }
    public static BufferedImage atlas(CapeDesign design){
        var image=new BufferedImage(ATLAS_WIDTH,ATLAS_HEIGHT,BufferedImage.TYPE_INT_ARGB);
        CapeAtlas.paint(design,image::setRGB);
        return image;
    }
    public static byte[] encode(CapeDesign design) throws IOException {
        var out=new ByteArrayOutputStream();if(!ImageIO.write(atlas(design),"png",out))throw new IOException("PNG encoder unavailable");return out.toByteArray();
    }
    public static void write(Path path,CapeDesign design) throws IOException {AtomicFiles.write(path,encode(design));}
}
