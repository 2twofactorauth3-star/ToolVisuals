package ru.he3hauka.toolvisuals.network.friends;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class FriendWire {
    public static final String REQUEST_CHANNEL="toolvisuals:friends_request_v1",STATE_CHANNEL="toolvisuals:friends_state_v1";
    public static final int HEALTH=1,ARMOR=2,INVENTORY=4,MAX_FRIENDS=256,MAX_REQUEST=42,MAX_STATE=4096;
    public static final UUID NONE=new UUID(0,0);
    private static final java.util.regex.Pattern ITEM_KEY=java.util.regex.Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    public enum Operation { HELLO(0),CONTACT(1),REMOVE(2),KEEP(3),BYE(4);
        public final int id;Operation(int id){this.id=id;}
        static Operation byId(int id){for(var value:values())if(value.id==id)return value;throw new IllegalArgumentException("Unknown friend operation");}
    }
    public enum Kind { REPLY(0),STATUS(1),INVENTORY(2),CLEAR(3);
        public final int id;Kind(int id){this.id=id;}
        static Kind byId(int id){for(var value:values())if(value.id==id)return value;throw new IllegalArgumentException("Unknown friend state");}
    }
    public record Request(UUID session,long id,Operation operation,UUID target,int fields){
        public Request{Objects.requireNonNull(session);Objects.requireNonNull(operation);Objects.requireNonNull(target);
            if(session.equals(NONE)||id<=0||fields<0||fields>7||(operation==Operation.CONTACT||operation==Operation.REMOVE)==target.equals(NONE)
                    ||operation!=Operation.CONTACT&&fields!=0)throw new IllegalArgumentException("Invalid friend request");}
    }
    public record Item(String key,int count,int damage,int maximum,boolean enchanted){
        public static final Item EMPTY=new Item("",0,0,0,false);
        public Item{Objects.requireNonNull(key);
            if(count<0||count>127||damage<0||maximum<0||maximum>1_000_000||damage>maximum
                    ||count==0&&(!key.isEmpty()||damage!=0||maximum!=0||enchanted)
                    ||count>0&&(key.length()>64||!ITEM_KEY.matcher(key).matches()))throw new IllegalArgumentException("Invalid friend item");}
    }
    public record State(UUID server,UUID session,UUID actor,long sequence,Kind kind,int fields,float health,float maximum,float absorption,List<Item> items){
        public State{Objects.requireNonNull(server);Objects.requireNonNull(session);Objects.requireNonNull(actor);Objects.requireNonNull(kind);items=List.copyOf(items);
            if(session.equals(NONE)||sequence<=0||fields<0||fields>7||!Float.isFinite(health)||!Float.isFinite(maximum)||!Float.isFinite(absorption)
                    ||health<0||maximum<0||health>maximum||maximum>2048||absorption<0||absorption>2048)throw new IllegalArgumentException("Invalid friend state");
            boolean valid=switch(kind){
                case REPLY->actor.equals(NONE)&&fields<=1&&items.isEmpty();
                case CLEAR->!actor.equals(NONE)&&fields==0&&items.isEmpty();
                case STATUS->!actor.equals(NONE)&&(fields&~3)==0&&items.size()==((fields&ARMOR)!=0?4:0)&&((fields&HEALTH)!=0||maximum==0&&health==0&&absorption==0);
                case INVENTORY->!actor.equals(NONE)&&fields==INVENTORY&&items.size()==37;
            };
            if(!valid||kind!=Kind.STATUS&&(health!=0||maximum!=0||absorption!=0))throw new IllegalArgumentException("Invalid friend state contents");}
    }
    private FriendWire(){}
    private static void uuid(ByteBuffer out,UUID value){out.putLong(value.getMostSignificantBits()).putLong(value.getLeastSignificantBits());}
    private static UUID uuid(ByteBuffer in){return new UUID(in.getLong(),in.getLong());}
    public static byte[] encode(Request request){var out=ByteBuffer.allocate(MAX_REQUEST);uuid(out,request.session);out.putLong(request.id).put((byte)request.operation.id);uuid(out,request.target);out.put((byte)request.fields);return out.array();}
    public static Request request(byte[] bytes){if(bytes==null||bytes.length!=MAX_REQUEST)throw new IllegalArgumentException("Invalid friend request size");var in=ByteBuffer.wrap(bytes);return new Request(uuid(in),in.getLong(),Operation.byId(Byte.toUnsignedInt(in.get())),uuid(in),Byte.toUnsignedInt(in.get()));}
    public static int size(State value){
        int size=58+(value.kind==Kind.STATUS?12:0);for(var item:value.items)size+=11+item.key.length();
        return size;
    }
    public static byte[] encode(State value){
        var out=ByteBuffer.allocate(size(value));uuid(out,value.server);uuid(out,value.session);uuid(out,value.actor);out.putLong(value.sequence).put((byte)value.kind.id).put((byte)value.fields);
        if(value.kind==Kind.STATUS)out.putFloat(value.health).putFloat(value.maximum).putFloat(value.absorption);
        for(var item:value.items){byte[] key=item.key.getBytes(StandardCharsets.US_ASCII);out.put((byte)key.length).put(key).put((byte)item.count).putInt(item.damage).putInt(item.maximum).put((byte)(item.enchanted?1:0));}
        return out.array();
    }
    public static State state(byte[] bytes){
        if(bytes==null||bytes.length<58||bytes.length>MAX_STATE)throw new IllegalArgumentException("Invalid friend state size");
        var in=ByteBuffer.wrap(bytes);UUID server=uuid(in),session=uuid(in),actor=uuid(in);long sequence=in.getLong();var kind=Kind.byId(Byte.toUnsignedInt(in.get()));int fields=Byte.toUnsignedInt(in.get());
        float health=0,maximum=0,absorption=0;if(kind==Kind.STATUS){health=in.getFloat();maximum=in.getFloat();absorption=in.getFloat();}
        int count=kind==Kind.INVENTORY?37:kind==Kind.STATUS&&(fields&ARMOR)!=0?4:0;var items=new ArrayList<Item>(count);
        for(int i=0;i<count;i++){int length=Byte.toUnsignedInt(in.get());if(length>64||in.remaining()<length+10)throw new IllegalArgumentException("Invalid friend item size");byte[] key=new byte[length];in.get(key);int amount=Byte.toUnsignedInt(in.get()),damage=in.getInt(),max=in.getInt(),glint=Byte.toUnsignedInt(in.get());if(glint>1)throw new IllegalArgumentException("Invalid friend item flag");items.add(new Item(new String(key,StandardCharsets.US_ASCII),amount,damage,max,glint==1));}
        if(in.hasRemaining())throw new IllegalArgumentException("Trailing friend state bytes");return new State(server,session,actor,sequence,kind,fields,health,maximum,absorption,items);
    }
    public static State reply(UUID server,Request request,boolean accepted){return new State(server,request.session,NONE,request.id,Kind.REPLY,accepted?1:0,0,0,0,List.of());}
}
