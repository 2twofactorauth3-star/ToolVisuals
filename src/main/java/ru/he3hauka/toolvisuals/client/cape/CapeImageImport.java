package ru.he3hauka.toolvisuals.client.cape;

import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeAnimationPolicy;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

public final class CapeImageImport {
    public enum Layout {
        SINGLE(20,32),DUAL(40,32),VANILLA(64,32),ATLAS(128,64);
        public final int width,height;
        Layout(int width,int height){this.width=width;this.height=height;}
        public String size(){return width+" × "+height;}
        public static Layout match(int width,int height){for(var layout:values())if(layout.width==width&&layout.height==height)return layout;return null;}
    }
    public final int width,height;
    public final Layout preferred;
    public final int sourceFrames,frameCount,fps;
    public final boolean adjusted;
    public static final class Frame {
        private final EnumMap<Layout,CapeDesign> layouts=new EnumMap<>(Layout.class);
        public Frame(BufferedImage image){for(var layout:Layout.values())layouts.put(layout,fitFrame(image,layout));}
    }
    private final List<Frame> frames;

    CapeImageImport(BufferedImage image) {
        this(image.getWidth(),image.getHeight(),List.of(new Frame(image)),CapeDesign.DEFAULT_FPS,1,false);
    }

    public CapeImageImport(int width,int height,List<Frame> frames,int fps,int sourceFrames,boolean adjusted) {
        if(width<1||height<1||frames==null||frames.isEmpty()||frames.size()>CapeAnimationPolicy.MAX_FRAMES||fps<1||fps>CapeDesign.MAX_FPS||frames.size()>1&&fps>CapeAnimationPolicy.MAX_FPS)throw new IllegalArgumentException("Invalid imported cape frames");
        this.width=width;this.height=height;this.frames=List.copyOf(frames);this.fps=fps;this.sourceFrames=sourceFrames;this.frameCount=frames.size();this.adjusted=adjusted;
        var exact=Layout.match(width,height);
        preferred=exact==null?nearest(width,height):exact;
    }

    private static Layout nearest(int width,int height) {
        double best=Double.POSITIVE_INFINITY;Layout selected=Layout.SINGLE;
        for(var layout:Layout.values()) {
            double aspect=Math.abs(Math.log((double)width/height/((double)layout.width/layout.height)));
            double scale=Math.abs(Math.log((double)width/layout.width))+Math.abs(Math.log((double)height/layout.height));
            double cost=aspect*8+scale;
            if(cost<best){best=cost;selected=layout;}
        }
        return selected;
    }

    public boolean requiresResize(){return Layout.match(width,height)==null;}

    public CapeDesign fit(Layout layout) {
        if(layout==null)throw new IllegalArgumentException("Missing cape layout");
        var designs=frames.stream().map(frame->frame.layouts.get(layout)).toList();
        if(designs.size()==1||designs.stream().allMatch(designs.getFirst()::equals))return designs.getFirst();
        return new CapeDesign(designs,fps);
    }

    private static CapeDesign fitFrame(BufferedImage image,Layout layout) {
        if(layout==null)throw new IllegalArgumentException("Missing cape layout");
        int width=image.getWidth(),height=image.getHeight();
        if(layout.width==width&&layout.height==height)return CapePng.fromImage(image);
        var resized=new BufferedImage(layout.width,layout.height,BufferedImage.TYPE_INT_ARGB_PRE);
        var graphics=resized.createGraphics();
        try {
            boolean integerScale=(width%layout.width==0&&height%layout.height==0&&width/layout.width==height/layout.height)
                    ||(layout.width%width==0&&layout.height%height==0&&layout.width/width==layout.height/height);
            graphics.setComposite(AlphaComposite.Src);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,integerScale?RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR:RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.drawImage(image,0,0,layout.width,layout.height,null);
        } finally { graphics.dispose(); }
        return CapePng.fromImage(resized);
    }
}
