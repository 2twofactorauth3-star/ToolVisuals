package ru.he3hauka.toolvisuals.cosmetic.cape;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public final class CapeDesign {
    public static final int WIDTH=20, HEIGHT=32, FACE_PIXELS=WIDTH*HEIGHT, PIXELS=FACE_PIXELS*2, BYTES=PIXELS*4;
    public static final int MAX_FRAMES=6,DEFAULT_FPS=8,MAX_FPS=20,MAX_ANIMATION_BYTES=2+MAX_FRAMES*BYTES;
    public enum Face { FRONT, BACK }
    private final int[] pixels;
    private final List<CapeDesign> frames;
    public final int frameCount,fps;
    public final String hash;
    public CapeDesign(int[] pixels){
        if(pixels==null||pixels.length!=PIXELS)throw new IllegalArgumentException("Invalid cape dimensions");
        this.pixels=pixels.clone();
        frames=List.of();frameCount=1;fps=DEFAULT_FPS;hash=digest(animationBytes());
    }
    public CapeDesign(List<CapeDesign> frames,int fps){
        if(frames==null||frames.isEmpty()||frames.size()>MAX_FRAMES||fps<1||fps>MAX_FPS)throw new IllegalArgumentException("Invalid cape animation");
        for(var frame:frames)if(Objects.requireNonNull(frame).frameCount!=1)throw new IllegalArgumentException("Nested cape animation");
        this.frames=List.copyOf(frames);frameCount=frames.size();this.fps=fps;pixels=frames.get(0).pixels;hash=digest(animationBytes());
    }
    private static String digest(byte[] bytes){
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
        catch(NoSuchAlgorithmException error){throw new IllegalStateException(error);}
    }
    public CapeDesign frame(int index){if(index<0||index>=frameCount)throw new IllegalArgumentException("Invalid cape frame");return frames.isEmpty()?this:frames.get(index);}
    public CapeDesign at(long ticks,float partial){
        long seconds=Math.floorDiv(ticks,20);double fraction=(Math.floorMod(ticks,20)+Math.max(0,Math.min(1,Float.isFinite(partial)?partial:0)))/20;
        return frame((int)((Math.floorMod(seconds,frameCount)*fps+(long)Math.floor(fraction*fps))%frameCount));
    }
    public int pixel(int index){return pixels[index];}
    public int pixel(Face face,int x,int y){return pixels[index(face,x,y)];}
    public static int index(Face face,int x,int y){
        if(face==null||x<0||x>=WIDTH||y<0||y>=HEIGHT)throw new IllegalArgumentException("Outside cape canvas");
        return (face==Face.BACK?FACE_PIXELS:0)+y*WIDTH+x;
    }
    public int[] pixels(){return pixels.clone();}
    public byte[] bytes(){var buffer=ByteBuffer.allocate(BYTES);for(int pixel:pixels)buffer.putInt(pixel);return buffer.array();}
    public byte[] animationBytes(){var buffer=ByteBuffer.allocate(2+frameCount*BYTES);buffer.put((byte)frameCount).put((byte)fps);for(int i=0;i<frameCount;i++)buffer.put(frame(i).bytes());return buffer.array();}
    public static CapeDesign animation(byte[] bytes){
        if(bytes==null||bytes.length<2||bytes.length>MAX_ANIMATION_BYTES)throw new IllegalArgumentException("Invalid cape animation size");
        var buffer=ByteBuffer.wrap(bytes);int count=Byte.toUnsignedInt(buffer.get()),fps=Byte.toUnsignedInt(buffer.get());
        if(count<1||count>MAX_FRAMES||fps<1||fps>MAX_FPS||buffer.remaining()!=count*BYTES)throw new IllegalArgumentException("Invalid cape animation frames");
        var frames=new ArrayList<CapeDesign>(count);for(int i=0;i<count;i++){byte[] frame=new byte[BYTES];buffer.get(frame);frames.add(decode(frame));}return new CapeDesign(frames,fps);
    }
    public static CapeDesign decode(byte[] bytes){
        if(bytes==null||bytes.length!=BYTES)throw new IllegalArgumentException("Invalid cape size");
        var buffer=ByteBuffer.wrap(bytes);var pixels=new int[PIXELS];for(int i=0;i<PIXELS;i++)pixels[i]=buffer.getInt();return new CapeDesign(pixels);
    }
    @Override public boolean equals(Object value){
        if(!(value instanceof CapeDesign cape)||frameCount!=cape.frameCount||fps!=cape.fps)return false;
        for(int i=0;i<frameCount;i++)if(!Arrays.equals(frame(i).pixels,cape.frame(i).pixels))return false;return true;
    }
    @Override public int hashCode(){return hash.hashCode();}
}
