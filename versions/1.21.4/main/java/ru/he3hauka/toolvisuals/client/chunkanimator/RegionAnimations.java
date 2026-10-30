package ru.he3hauka.toolvisuals.client.chunkanimator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public final class RegionAnimations {
    private final boolean[] loaded=new boolean[256];
    private final long[] starts=new long[256];
    private final FloatBuffer times=ByteBuffer.allocateDirect(256*Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
    public RegionAnimations(){Arrays.fill(starts,Long.MIN_VALUE);}
    public void uploaded(int section){long now=System.nanoTime();if(ChunkAnimations.enabled()&&(!loaded[section]||ChunkAnimations.animateUpdates()&&(starts[section]==Long.MIN_VALUE||now-starts[section]>=ChunkAnimations.durationMillis()*1_000_000L)))starts[section]=now;loaded[section]=true;}
    public void forget(int section){loaded[section]=false;starts[section]=Long.MIN_VALUE;}
    public FloatBuffer times(){long now=System.nanoTime(),duration=ChunkAnimations.durationMillis()*1_000_000L;for(int i=0;i<256;i++)times.put(i,starts[i]!=Long.MIN_VALUE&&now-starts[i]<duration?millis(starts[i]):-1);return times;}
    public static int millis(long nanos){return (int)(nanos/1_000_000L)&0x3fffff;}
}
