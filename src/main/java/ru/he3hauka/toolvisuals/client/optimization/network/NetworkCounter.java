package ru.he3hauka.toolvisuals.client.optimization.network;

public final class NetworkCounter {
    public record Snapshot(long frames,long sentBytes,long vanillaBytes,long avoidedFrameCopyBytes) {}
    public static final Snapshot EMPTY=new Snapshot(0,0,0,0);
    private long frames,sentBytes,avoidedFrameCopyBytes;

    public synchronized void sent(int payloadBytes){
        if(payloadBytes<0||payloadBytes>0x1fffff)throw new IllegalArgumentException("Invalid frame length");
        frames++;sentBytes+=payloadBytes+PacketBytes.size(payloadBytes);
    }
    public synchronized void avoidedCopy(int payloadBytes){
        if(payloadBytes<0||payloadBytes>0x1fffff)throw new IllegalArgumentException("Invalid frame length");
        avoidedFrameCopyBytes+=payloadBytes;
    }
    public synchronized Snapshot snapshot(){return new Snapshot(frames,sentBytes,sentBytes,avoidedFrameCopyBytes);}
}
