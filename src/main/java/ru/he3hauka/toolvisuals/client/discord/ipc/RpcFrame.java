package ru.he3hauka.toolvisuals.client.discord.ipc;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonObject;
import ru.he3hauka.toolvisuals.client.storage.BoundedJson;

public final class RpcFrame {
    public static final int HANDSHAKE=0,FRAME=1,CLOSE=2,PING=3,PONG=4,MAX_BYTES=64*1024,MAX_OUTGOING=4096;
    private static final BoundedJson.Limits JSON_LIMITS=new BoundedJson.Limits(MAX_BYTES,2048);
    public final int opcode;
    private final byte[] payload;
    public RpcFrame(int opcode,byte[] payload){if(opcode<0||opcode>PONG||payload==null||payload.length>MAX_BYTES)throw new IllegalArgumentException("Invalid Discord IPC frame");this.opcode=opcode;this.payload=payload.clone();}
    public static RpcFrame json(int opcode,JsonObject value){return new RpcFrame(opcode,value.toString().getBytes(StandardCharsets.UTF_8));}
    public byte[] payload(){return payload.clone();}
    public JsonObject json() throws IOException {
        try{return BoundedJson.parse(StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(payload)).toString(),JSON_LIMITS).getAsJsonObject();}
        catch(RuntimeException error){throw new IOException("Invalid Discord IPC JSON",error);}
    }
    public ByteBuffer encode(){return ByteBuffer.allocate(8+payload.length).order(ByteOrder.LITTLE_ENDIAN).putInt(opcode).putInt(payload.length).put(payload).flip();}

    public static final class Reader {
        private final ByteBuffer header=ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        private ByteBuffer body;
        private int opcode;
        public RpcFrame poll(RpcTransport transport) throws IOException {
            if(body==null){if(read(transport,header))return null;header.flip();opcode=header.getInt();int length=header.getInt();header.clear();if(opcode<0||opcode>PONG||length<0||length>MAX_BYTES)throw new IOException("Invalid Discord IPC frame header");body=ByteBuffer.allocate(length);}
            if(body.hasRemaining()&&read(transport,body))return null;
            var frame=new RpcFrame(opcode,body.array());body=null;return frame;
        }
        private static boolean read(RpcTransport transport,ByteBuffer target) throws IOException {if(transport.read(target)<0)throw new EOFException("Discord IPC closed");return target.hasRemaining();}
    }
}
