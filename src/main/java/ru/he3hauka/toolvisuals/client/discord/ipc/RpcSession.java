package ru.he3hauka.toolvisuals.client.discord.ipc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import ru.he3hauka.toolvisuals.client.discord.DiscordActivity;

public final class RpcSession {
    private static final long HANDSHAKE_TIMEOUT=5_000_000_000L,ACK_TIMEOUT=10_000_000_000L,UPDATE_INTERVAL=5_000_000_000L,HEARTBEAT_INTERVAL=20_000_000_000L;
    private static final long REPUBLISH_INTERVAL=60_000_000_000L;
    private final RpcTransport transport;
    private final RpcFrame.Reader reader=new RpcFrame.Reader();
    private final long pid,created;
    private final String identity=UUID.randomUUID().toString();
    private long sequence,queuedAt,pendingSince,lastSent=Long.MIN_VALUE,lastTraffic,closingSince;
    private ByteBuffer outgoing;
    private RpcFrame pong;
    private byte[] heartbeat;
    private long heartbeatSince;
    private String pendingNonce;
    private DiscordActivity pendingActivity,acknowledged;
    private boolean ready,closing,clearSent,finished;

    public static final class Rejected extends IOException {
        public final int code;
        private Rejected(JsonObject data){super("Discord RPC rejection: "+description(data));code=number(data,"code");}
        private static int number(JsonObject data,String key){var value=data.get(key);return value!=null&&value.isJsonPrimitive()&&value.getAsJsonPrimitive().isNumber()?value.getAsInt():-1;}
        private static String description(JsonObject data){
            String message=text(data,"message").replaceAll("[\\p{Cntrl}\\p{Cf}]"," ").strip();
            if(message.length()>256)message=message.substring(0,256);
            return "code="+number(data,"code")+" message="+(message.isEmpty()?"not supplied":message);
        }
    }
    public RpcSession(RpcTransport transport,String applicationId,long pid,long now) throws IOException {
        this.transport=transport;this.pid=pid;created=lastTraffic=now;
        if(applicationId==null||!applicationId.matches("[1-9][0-9]{14,19}")||pid<1)throw new IllegalArgumentException("Invalid Discord application identity");
        var hello=new JsonObject();hello.addProperty("v",1);hello.addProperty("client_id",applicationId);enqueue(RpcFrame.json(RpcFrame.HANDSHAKE,hello),now);
    }
    public boolean published(){return ready&&acknowledged!=null&&!closing;}
    public boolean pump(DiscordActivity activity,long now) throws IOException {
        if(activity==null&&!closing){closing=true;closingSince=now;if(outgoing!=null&&outgoing.position()==0&&ready){outgoing=null;pendingNonce=null;}}
        if(finished||closing&&(!ready||now-closingSince>=1_000_000_000L))return false;
        if(!transport.connected()){if(now-created>=HANDSHAKE_TIMEOUT)throw new IOException("Discord IPC connect timeout");return true;}
        flush(now);
        for(int count=0;count<8;count++){var frame=reader.poll(transport);if(frame==null)break;lastTraffic=now;receive(frame);if(finished)return false;}
        if(!ready&&now-created>=HANDSHAKE_TIMEOUT)throw new IOException("Discord IPC handshake timeout");
        if(pendingNonce!=null&&now-pendingSince>=ACK_TIMEOUT)throw new IOException("Discord IPC acknowledgement timeout");
        if(heartbeat!=null&&now-heartbeatSince>=HANDSHAKE_TIMEOUT)throw new IOException("Discord IPC heartbeat timeout");
        if(outgoing!=null)return true;
        if(pong!=null){enqueue(pong,now);pong=null;}
        else if(ready&&closing&&!clearSent){clearSent=true;command(null,now);}
        else if(ready&&!closing&&pendingNonce==null&&publicationDue(activity,now))command(activity,now);
        else if(ready&&!closing&&pendingNonce==null&&heartbeat==null&&now-lastTraffic>=HEARTBEAT_INTERVAL){heartbeat=identity.getBytes(java.nio.charset.StandardCharsets.UTF_8);heartbeatSince=now;enqueue(new RpcFrame(RpcFrame.PING,heartbeat),now);}
        flush(now);return true;
    }
    private boolean publicationDue(DiscordActivity activity,long now){
        if(lastSent==Long.MIN_VALUE)return true;
        long elapsed=now-lastSent;
        return elapsed>=UPDATE_INTERVAL&&(!activity.equals(acknowledged)||elapsed>=REPUBLISH_INTERVAL);
    }
    private void receive(RpcFrame frame) throws IOException {
        if(frame.opcode==RpcFrame.CLOSE)throw new Rejected(frame.json());
        if(frame.opcode==RpcFrame.PING){if(frame.payload().length>RpcFrame.MAX_OUTGOING||pong!=null)throw new IOException("Discord IPC ping limit");pong=new RpcFrame(RpcFrame.PONG,frame.payload());return;}
        if(frame.opcode==RpcFrame.PONG){if(heartbeat!=null&&Arrays.equals(heartbeat,frame.payload()))heartbeat=null;return;}
        if(frame.opcode!=RpcFrame.FRAME)throw new IOException("Unexpected Discord IPC opcode");
        var value=frame.json();String event=text(value,"evt"),command=text(value,"cmd"),nonce=text(value,"nonce");
        if(event.equals("ERROR")&&(!ready||nonce.equals(pendingNonce))){
            var data=value.get("data");throw new Rejected(data!=null&&data.isJsonObject()?data.getAsJsonObject():new JsonObject());
        }
        if(!ready&&event.equals("READY")&&command.equals("DISPATCH")){ready=true;return;}
        if(ready&&command.equals("SET_ACTIVITY")&&pendingNonce!=null&&pendingNonce.equals(nonce)){
            acknowledged=pendingActivity;pendingNonce=null;pendingActivity=null;if(closing&&clearSent)finished=true;
        }
    }
    private static String text(JsonObject object,String field){var value=object.get(field);return value!=null&&value.isJsonPrimitive()?value.getAsString():"";}
    private void command(DiscordActivity activity,long now) throws IOException {
        pendingNonce=identity+":"+(++sequence);pendingActivity=activity;pendingSince=lastSent=now;
        var value=new JsonObject();value.addProperty("cmd","SET_ACTIVITY");value.addProperty("nonce",pendingNonce);
        var arguments=new JsonObject();arguments.addProperty("pid",pid);arguments.add("activity",activity==null?JsonNull.INSTANCE:activity.json());value.add("args",arguments);enqueue(RpcFrame.json(RpcFrame.FRAME,value),now);
    }
    private void enqueue(RpcFrame frame,long now) throws IOException {var buffer=frame.encode();if(buffer.remaining()>RpcFrame.MAX_OUTGOING+8)throw new IOException("Discord IPC output exceeds limit");outgoing=buffer;queuedAt=now;}
    private void flush(long now) throws IOException {if(outgoing==null)return;if(now-queuedAt>=HANDSHAKE_TIMEOUT)throw new IOException("Discord IPC write timeout");transport.write(outgoing);if(!outgoing.hasRemaining())outgoing=null;}
}
