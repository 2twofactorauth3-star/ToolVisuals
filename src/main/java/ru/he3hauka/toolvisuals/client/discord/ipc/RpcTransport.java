package ru.he3hauka.toolvisuals.client.discord.ipc;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.*;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public interface RpcTransport extends AutoCloseable {
    boolean connected() throws IOException;
    int read(ByteBuffer target) throws IOException;
    void write(ByteBuffer source) throws IOException;
    @Override void close() throws IOException;

    final class Factory {
        private final boolean windows=System.getProperty("os.name","").toLowerCase(Locale.ROOT).startsWith("windows");
        private final List<String> endpoints=new ArrayList<>();
        private int cursor;
        public Factory(){this("discord-ipc-");}
        Factory(String namespace){
            if(windows){for(int i=0;i<10;i++)endpoints.add("\\\\.\\pipe\\"+namespace+i);}
            else {
                var directories=new LinkedHashSet<String>();for(String key:List.of("XDG_RUNTIME_DIR","TMPDIR","TMP","TEMP")){String value=System.getenv(key);if(value!=null&&!value.isBlank())directories.add(value);}directories.add("/tmp");
                for(String directory:directories)try{var path=Path.of(directory);if(path.isAbsolute())for(int i=0;i<10;i++)endpoints.add(path.resolve(namespace+i).toString());}catch(java.nio.file.InvalidPathException error){ClientErrors.report("Discord RPC endpoint discovery",error);}
            }
        }
        public RpcTransport open() throws IOException {
            IOException failure=null;
            for(int count=0;count<endpoints.size();count++){
                String endpoint=endpoints.get(cursor);cursor=(cursor+1)%endpoints.size();
                try{return windows?new WindowsPipe(endpoint):new UnixSocket(Path.of(endpoint));}
                catch(IOException error){if(failure==null)failure=error;}
                catch(UnsupportedOperationException error){throw new IOException("Discord IPC transport is not supported",error);}
            }
            throw new IOException("Discord desktop IPC is unavailable; checked "+endpoints.size()+" local endpoints",failure);
        }
    }

    final class WindowsPipe implements RpcTransport {
        private final RandomAccessFile pipe;
        private final FileInputStream input;
        private final byte[] buffer=new byte[4096];
        private final AtomicBoolean closed=new AtomicBoolean();
        WindowsPipe(String path) throws IOException {pipe=new RandomAccessFile(path,"rw");input=new FileInputStream(pipe.getFD());}
        @Override public boolean connected(){return !closed.get();}
        @Override public int read(ByteBuffer target) throws IOException {
            int count=Math.min(Math.min(input.available(),target.remaining()),buffer.length);if(count==0)return 0;
            int read=input.read(buffer,0,count);if(read>0)target.put(buffer,0,read);return read;
        }
        @Override public void write(ByteBuffer source) throws IOException {int count=source.remaining();pipe.write(source.array(),source.arrayOffset()+source.position(),count);source.position(source.position()+count);}
        @Override public void close() throws IOException {if(closed.compareAndSet(false,true))pipe.close();}
    }

    final class UnixSocket implements RpcTransport {
        private final SocketChannel socket;
        UnixSocket(Path path) throws IOException {
            socket=SocketChannel.open(StandardProtocolFamily.UNIX);try{socket.configureBlocking(false);socket.connect(UnixDomainSocketAddress.of(path));}catch(IOException|RuntimeException error){socket.close();throw error;}
        }
        @Override public boolean connected() throws IOException {return socket.isConnected()||socket.finishConnect();}
        @Override public int read(ByteBuffer target) throws IOException {return socket.read(target);}
        @Override public void write(ByteBuffer source) throws IOException {socket.write(source);}
        @Override public void close() throws IOException {socket.close();}
    }
}
