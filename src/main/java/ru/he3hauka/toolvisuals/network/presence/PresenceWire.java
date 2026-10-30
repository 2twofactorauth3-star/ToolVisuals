package ru.he3hauka.toolvisuals.network.presence;

import java.nio.ByteBuffer;
import java.util.*;

public final class PresenceWire {
    public static final String REQUEST_CHANNEL="toolvisuals:client_list_request_v1";
    public static final String STATE_CHANNEL="toolvisuals:client_list_state_v1";
    public static final int MAX_PLAYERS=2048,PAGE_SIZE=128,HEADER_BYTES=36,MAX_BYTES=HEADER_BYTES+PAGE_SIZE*16;

    public record Page(UUID server,long epoch,long revision,int total,int index,List<UUID> players){
        public Page{
            Objects.requireNonNull(server);
            if(epoch<=0||revision<=0||total<0||total>MAX_PLAYERS||index<0||index>=pages(total))throw new IllegalArgumentException("Invalid presence snapshot");
            players=List.copyOf(players);
            if(players.size()!=count(total,index)||new HashSet<>(players).size()!=players.size())throw new IllegalArgumentException("Invalid presence page");
        }
    }

    private PresenceWire(){}
    public static int pages(int total){return Math.max(1,(total+PAGE_SIZE-1)/PAGE_SIZE);}
    private static int count(int total,int index){return Math.min(PAGE_SIZE,total-index*PAGE_SIZE);}
    private static void uuid(ByteBuffer buffer,UUID value){buffer.putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits());}
    private static UUID uuid(ByteBuffer buffer){return new UUID(buffer.getLong(),buffer.getLong());}

    public enum Subscription {
        OFF(0),START(1),KEEP(2);
        public final int id;
        Subscription(int id){this.id=id;}
    }
    public static byte[] subscription(Subscription value){return new byte[]{(byte)value.id};}
    public static Subscription subscription(byte[] bytes){
        if(bytes==null||bytes.length!=1)throw new IllegalArgumentException("Invalid presence subscription");
        return switch(bytes[0]){case 0->Subscription.OFF;case 1->Subscription.START;case 2->Subscription.KEEP;default->throw new IllegalArgumentException("Invalid presence subscription");};
    }

    public static byte[] encode(Page page){
        var buffer=ByteBuffer.allocate(HEADER_BYTES+page.players.size()*16);
        uuid(buffer,page.server);buffer.putLong(page.epoch).putLong(page.revision).putShort((short)page.total).putShort((short)page.index);
        for(var player:page.players)uuid(buffer,player);
        return buffer.array();
    }

    public static Page decode(byte[] bytes){
        if(bytes==null||bytes.length<HEADER_BYTES||bytes.length>MAX_BYTES)throw new IllegalArgumentException("Invalid presence payload length");
        var buffer=ByteBuffer.wrap(bytes);
        UUID server=uuid(buffer);long epoch=buffer.getLong(),revision=buffer.getLong();
        int total=Short.toUnsignedInt(buffer.getShort()),index=Short.toUnsignedInt(buffer.getShort());
        if(total>MAX_PLAYERS||index>=pages(total)||bytes.length!=HEADER_BYTES+count(total,index)*16)throw new IllegalArgumentException("Invalid presence page length");
        var players=new ArrayList<UUID>(count(total,index));while(buffer.hasRemaining())players.add(uuid(buffer));
        return new Page(server,epoch,revision,total,index,players);
    }

    public static List<Page> snapshot(UUID server,long epoch,long revision,Collection<UUID> players){
        if(players.size()>MAX_PLAYERS)throw new IllegalArgumentException("Too many presence entries");
        var sorted=players.stream().sorted().toList();
        if(new HashSet<>(sorted).size()!=sorted.size())throw new IllegalArgumentException("Duplicate presence entries");
        var result=new ArrayList<Page>(pages(sorted.size()));
        for(int index=0;index<pages(sorted.size());index++){
            int from=index*PAGE_SIZE,to=Math.min(sorted.size(),from+PAGE_SIZE);
            result.add(new Page(server,epoch,revision,sorted.size(),index,sorted.subList(from,to)));
        }
        return List.copyOf(result);
    }
}
