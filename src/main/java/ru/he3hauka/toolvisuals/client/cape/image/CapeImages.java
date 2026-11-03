package ru.he3hauka.toolvisuals.client.cape.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import ru.he3hauka.toolvisuals.client.cape.CapeImageImport;
import ru.he3hauka.toolvisuals.client.cape.CapePng;
import ru.he3hauka.toolvisuals.client.storage.BoundedInputStream;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;
import ru.he3hauka.toolvisuals.storage.AtomicFiles;

public final class CapeImages {
    public static final String EXTENSIONS="png;jpg;jpeg;jfif;bmp;dib;gif;tif;tiff;wbmp;webp";
    private static final Set<String> FORMATS=Set.of("png","jpeg","jpg","bmp","gif","tiff","tif","wbmp","webp");
    private CapeImages() {}

    public static CapeImageImport read(Path path) throws IOException {
        byte[] bytes;
        try(var input=new BoundedInputStream(Files.newInputStream(path),CapePng.MAX_IMPORT_BYTES)){bytes=input.readAllBytes();}
        if(bytes.length<8)throw new IOException("Invalid cape image");
        try(var input=new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers=ImageIO.getImageReaders(input);
            ImageReader reader=readers.hasNext()?readers.next():webp(bytes);
            if(reader==null)throw new IOException("Unsupported cape image format");
            try {
                reader.setInput(input,false,false);
                String format=reader.getFormatName().toLowerCase(Locale.ROOT);
                if(!FORMATS.contains(format))throw new IOException("Unsupported cape image format: "+format);
                dimensions(reader.getWidth(0),reader.getHeight(0));
                if(format.equals("gif"))return GifCapeImport.read(reader);
                BufferedImage image=reader.read(0);
                try{dimensions(image.getWidth(),image.getHeight());return new CapeImageImport(image.getWidth(),image.getHeight(),java.util.List.of(new CapeImageImport.Frame(image)),CapeDesign.DEFAULT_FPS,1,false);}
                finally{image.flush();}
            } finally {reader.dispose();}
        } catch(RuntimeException error){throw new IOException("Invalid cape image",error);}
    }

    private static ImageReader webp(byte[] bytes) throws IOException {
        if(bytes.length<12||bytes[0]!='R'||bytes[1]!='I'||bytes[2]!='F'||bytes[3]!='F'||bytes[8]!='W'||bytes[9]!='E'||bytes[10]!='B'||bytes[11]!='P')return null;
        try{return ((ImageReaderSpi)Class.forName("com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi").getConstructor().newInstance()).createReaderInstance();}
        catch(ReflectiveOperationException|LinkageError error){throw new IOException("WebP codec unavailable",error);}
    }

    static void dimensions(int width,int height) throws IOException {
        if(width<1||height<1||width>CapePng.MAX_IMPORT_SIDE||height>CapePng.MAX_IMPORT_SIDE||(long)width*height>CapePng.MAX_IMPORT_PIXELS)throw new IOException("Cape image dimensions exceed import limits");
    }

    public static void write(Path path,CapeDesign design) throws IOException {
        if(!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".gif")){CapePng.write(path,design);return;}
        var writers=ImageIO.getImageWritersByFormatName("gif");
        if(!writers.hasNext())throw new IOException("GIF encoder unavailable");
        var writer=writers.next();var bytes=new ByteArrayOutputStream();
        try(var output=new MemoryCacheImageOutputStream(bytes)) {
            writer.setOutput(output);writer.prepareWriteSequence(null);
            for(int i=0;i<design.frameCount;i++) {
                var image=CapePng.atlas(design.frame(i));
                try {
                    var metadata=writer.getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image),writer.getDefaultWriteParam());
                    var root=(IIOMetadataNode)metadata.getAsTree("javax_imageio_gif_image_1.0");
                    var control=(IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
                    control.setAttribute("disposalMethod","restoreToBackgroundColor");control.setAttribute("userInputFlag","FALSE");
                    control.setAttribute("delayTime",Integer.toString(Math.max(1,Math.round(100f/design.fps))));
                    if(i==0&&design.frameCount>1){var extensions=new IIOMetadataNode("ApplicationExtensions");var loop=new IIOMetadataNode("ApplicationExtension");loop.setAttribute("applicationID","NETSCAPE");loop.setAttribute("authenticationCode","2.0");loop.setUserObject(new byte[]{1,0,0});extensions.appendChild(loop);root.appendChild(extensions);}
                    metadata.setFromTree("javax_imageio_gif_image_1.0",root);
                    writer.writeToSequence(new IIOImage(image,null,metadata),writer.getDefaultWriteParam());
                } finally {image.flush();}
            }
            writer.endWriteSequence();output.flush();
        } finally {writer.dispose();}
        AtomicFiles.write(path,bytes.toByteArray());
    }
}
