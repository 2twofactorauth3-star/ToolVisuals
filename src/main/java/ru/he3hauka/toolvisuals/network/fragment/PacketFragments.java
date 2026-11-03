package ru.he3hauka.toolvisuals.network.fragment;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PacketFragments {
    public static final int DATA_BYTES=4000,HEADER_BYTES=5,MAX_BYTES=32768;
    public static final long TIMEOUT=5_000_000_000L;

    public record Part(int total,int index,byte[] data) {
        public Part {check(total,index,Objects.requireNonNull(data).length);data=data.clone();}
        @Override public byte[] data(){return data.clone();}
        @Override public boolean equals(Object value){return value instanceof Part part&&total==part.total&&index==part.index&&Arrays.equals(data,part.data);}
        @Override public int hashCode(){return 31*(31*total+index)+Arrays.hashCode(data);}
        public int size(){return HEADER_BYTES+data.length;}
    }

    private PacketFragments(){}

    private static void check(int total,int index,int length){
        if(total<1||total>MAX_BYTES||index<0||index>=(total+DATA_BYTES-1)/DATA_BYTES
                ||length!=Math.min(DATA_BYTES,total-index*DATA_BYTES))throw new IllegalArgumentException("Invalid payload fragment");
    }

    public static List<Part> split(byte[] bytes){
        if(bytes==null||bytes.length<1||bytes.length>MAX_BYTES)throw new IllegalArgumentException("Invalid fragmented payload size");
        var result=new ArrayList<Part>((bytes.length+DATA_BYTES-1)/DATA_BYTES);
        for(int offset=0;offset<bytes.length;offset+=DATA_BYTES)result.add(new Part(bytes.length,offset/DATA_BYTES,Arrays.copyOfRange(bytes,offset,Math.min(bytes.length,offset+DATA_BYTES))));
        return List.copyOf(result);
    }

    public static void write(ByteBuffer buffer,Part part){buffer.putInt(part.total).put((byte)part.index).put(part.data);}

    public static Part read(ByteBuffer buffer,int maximum){
        if(buffer.remaining()<HEADER_BYTES||buffer.remaining()>HEADER_BYTES+DATA_BYTES)throw new IllegalArgumentException("Invalid fragment packet size");
        int total=buffer.getInt(),index=Byte.toUnsignedInt(buffer.get());
        check(total,index,buffer.remaining());
        if(total>maximum)throw new IllegalArgumentException("Fragmented payload exceeds its limit");
        byte[] data=new byte[buffer.remaining()];buffer.get(data);return new Part(total,index,data);
    }

    public static final class Assembler<K> {
        private static final class Entry {
            final long epoch,sequence,started;
            final byte[] bytes;
            int received;
            Entry(long epoch,long sequence,int total,long now){this.epoch=epoch;this.sequence=sequence;started=now;bytes=new byte[total];}
        }

        private final int limit;
        private final Map<K,Entry> entries=new HashMap<>();

        public Assembler(int limit){if(limit<1||limit>256)throw new IllegalArgumentException("Invalid fragment assembly limit");this.limit=limit;}
        private void expire(long now){entries.values().removeIf(entry->now-entry.started>=TIMEOUT);}
        public synchronized boolean contains(K key,long epoch,long sequence,long now){expire(now);var entry=entries.get(key);return entry!=null&&entry.epoch==epoch&&entry.sequence==sequence;}
        public synchronized byte[] accept(K key,long epoch,long sequence,Part part,long now){
            Objects.requireNonNull(key);Objects.requireNonNull(part);
            if(epoch<=0||sequence<0)throw new IllegalArgumentException("Invalid fragment identity");
            expire(now);var entry=entries.get(key);
            if(entry!=null){
                int order=Long.compare(epoch,entry.epoch);if(order==0)order=Long.compare(sequence,entry.sequence);
                if(order<0)return null;
                if(order>0){entries.remove(key);entry=null;}
            }
            if(entry==null){if(entries.size()>=limit)return null;entry=new Entry(epoch,sequence,part.total,now);entries.put(key,entry);}
            if(entry.bytes.length!=part.total)throw new IllegalArgumentException("Conflicting fragment size");
            int offset=part.index*DATA_BYTES,bit=1<<part.index;
            if((entry.received&bit)!=0){
                if(Arrays.mismatch(entry.bytes,offset,offset+part.data.length,part.data,0,part.data.length)>=0)throw new IllegalArgumentException("Conflicting repeated fragment");
                return null;
            }
            System.arraycopy(part.data,0,entry.bytes,offset,part.data.length);entry.received|=bit;
            if(entry.received!=(1<<((part.total+DATA_BYTES-1)/DATA_BYTES))-1)return null;
            entries.remove(key);return entry.bytes;
        }
        public synchronized void remove(K key){entries.remove(key);}
        public synchronized void remove(K key,long epoch){var entry=entries.get(key);if(entry!=null&&entry.epoch==epoch)entries.remove(key);}
        public synchronized void clear(){entries.clear();}
        public synchronized int size(){return entries.size();}
    }
}
