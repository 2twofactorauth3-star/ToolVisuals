package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

import java.util.Arrays;

public final class AccessoryMesh {
    @FunctionalInterface public interface Sink {void vertex(float x,float y,float z,int color,float shade);}
    @FunctionalInterface public interface ColoredSink {void vertex(float x,float y,float z,int color);}
    private final float[] vertices;
    private final byte[] colors;
    private final int offset;
    public final int size;
    private AccessoryMesh(float[] vertices,byte[] colors){this(vertices,colors,0,colors.length);}
    private AccessoryMesh(float[] vertices,byte[] colors,int offset,int size){this.vertices=vertices;this.colors=colors;this.offset=offset;this.size=size;}
    public AccessoryMesh slice(int from,int count){if(from<0||count<0||from%4!=0||count%4!=0||from+count>size)throw new IllegalArgumentException("Invalid mesh part");return new AccessoryMesh(vertices,colors,offset+from,count);}
    public void emit(Sink sink){for(int i=offset;i<offset+size;i++){int p=i*4;sink.vertex(vertices[p],vertices[p+1],vertices[p+2],Byte.toUnsignedInt(colors[i])>>>5,vertices[p+3]);}}
    public void emitColored(ColoredSink sink,int[] palette){for(int i=offset;i<offset+size;i++){int p=i*4;sink.vertex(vertices[p],vertices[p+1],vertices[p+2],palette[Byte.toUnsignedInt(colors[i])]);}}
    public static final class Builder {
        private float[] vertices=new float[1024];private byte[] colors=new byte[256];private int size;
        public void vertex(double x,double y,double z,int color,double shade){
            if(size>=8192||color<0||color>7||!Double.isFinite(shade)||shade<0||shade>1||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||Math.abs(x)>2||Math.abs(y)>2||Math.abs(z)>2)throw new IllegalArgumentException("Cosmetic mesh exceeds fixed model limits");
            if(size==colors.length){colors=Arrays.copyOf(colors,size*2);vertices=Arrays.copyOf(vertices,size*8);}
            int p=size*4;vertices[p]=(float)x;vertices[p+1]=(float)y;vertices[p+2]=(float)z;vertices[p+3]=(float)shade;colors[size++]=(byte)(color<<5|Math.clamp((int)Math.round(shade*31),0,31));
        }
        public void quad(double[] a,double[] b,double[] c,double[] d,int color,double shade){vertex(a[0],a[1],a[2],color,shade);vertex(b[0],b[1],b[2],color,shade);vertex(c[0],c[1],c[2],color,shade);vertex(d[0],d[1],d[2],color,shade);}
        public int mark(){return size;}
        public AccessoryMesh build(){if(size%4!=0)throw new IllegalStateException("Incomplete cosmetic quad");return new AccessoryMesh(Arrays.copyOf(vertices,size*4),Arrays.copyOf(colors,size));}
    }
}
