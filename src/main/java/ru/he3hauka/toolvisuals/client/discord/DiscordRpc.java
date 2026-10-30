package ru.he3hauka.toolvisuals.client.discord;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import ru.he3hauka.toolvisuals.client.discord.ipc.RpcSession;
import ru.he3hauka.toolvisuals.client.discord.ipc.RpcTransport;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

public final class DiscordRpc implements AutoCloseable {
    private static final org.slf4j.Logger LOGGER=org.slf4j.LoggerFactory.getLogger("ToolVisuals/DiscordRPC");
    public enum Status { DISABLED,WAITING,CONNECTING,CONNECTED,REJECTED }
    @FunctionalInterface public interface Opener { RpcTransport open() throws IOException; }

    private final Object lock=new Object();
    private final Opener opener;
    private final ScheduledThreadPoolExecutor worker=executor("ToolVisuals Discord RPC");
    private final ScheduledThreadPoolExecutor watchdog=executor("ToolVisuals Discord watchdog");
    private volatile Status status=Status.DISABLED;
    private volatile DiscordActivity desired;
    private volatile RpcTransport channel,stalled;
    private volatile long inIo;
    private boolean running,closed;
    private RpcSession session;
    private long retryAfter=Long.MIN_VALUE;
    private int failures;
    private ScheduledFuture<?> watch;
    private final Runnable stepTask=ClientErrors.task("Discord RPC worker",this::step);

    public DiscordRpc(Opener opener){this.opener=opener;}

    private static ScheduledThreadPoolExecutor executor(String name){
        var executor=new ScheduledThreadPoolExecutor(1,job->{
            var thread=new Thread(job,name);
            thread.setDaemon(true);
            return thread;
        });
        executor.setRemoveOnCancelPolicy(true);
        executor.setKeepAliveTime(30,TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public Status status(){return status;}

    public void publish(DiscordActivity activity){
        synchronized(lock){
            if(closed)return;
            desired=activity;
            if(activity!=null&&!running)start();
        }
    }

    private void start(){
        running=true;
        LOGGER.info("Starting Rich Presence: application_id={}, transport={}",DiscordBrand.APPLICATION_ID,System.getProperty("os.name","unknown"));
        watch=watchdog.scheduleWithFixedDelay(ClientErrors.task("Discord RPC watchdog",this::checkStall),1,1,TimeUnit.SECONDS);
        worker.execute(stepTask);
    }

    private void step(){
        try{
            long now=System.nanoTime();
            var activity=desired;
            if(session==null&&activity!=null&&(retryAfter==Long.MIN_VALUE||now-retryAfter>=0)){
                channel=opener.open();
                session=new RpcSession(channel,DiscordBrand.APPLICATION_ID,ProcessHandle.current().pid(),now);
                status=Status.CONNECTING;
            }
            if(session!=null){
                inIo=System.nanoTime();
                try{
                    if(!session.pump(activity,now)){
                        disconnect();
                        status=Status.DISABLED;
                    }else if(session.published()){
                        if(status!=Status.CONNECTED)LOGGER.info("Discord acknowledged ToolVisuals activity (application_id={})",DiscordBrand.APPLICATION_ID);
                        status=Status.CONNECTED;
                        failures=0;
                    }
                }finally{
                    inIo=0;
                }
            }else if(activity==null){
                status=Status.DISABLED;
            }
        }catch(IOException|RuntimeException error){
            ClientErrors.report("Discord RPC connect or publish (application_id="+DiscordBrand.APPLICATION_ID+")",error);
            disconnect();
            status=error instanceof RpcSession.Rejected?Status.REJECTED:Status.WAITING;
            failures=Math.min(4,failures+1);
            retryAfter=System.nanoTime()+Math.min(15,1L<<failures)*1_000_000_000L;
        }finally{
            scheduleNext();
        }
    }

    private void scheduleNext(){
        synchronized(lock){
            if(desired!=null||session!=null){
                worker.schedule(stepTask,250,TimeUnit.MILLISECONDS);
                return;
            }
            running=false;
            status=Status.DISABLED;
            if(watch!=null){
                watch.cancel(false);
                watch=null;
            }
            if(closed)shutdown();
        }
    }

    private void checkStall(){
        var active=channel;
        long since=inIo;
        if(active==null||active==stalled||since==0||System.nanoTime()-since<5_000_000_000L)return;
        stalled=active;
        status=Status.WAITING;
        ClientErrors.report("Discord RPC stalled transport",new IOException("Local Discord IPC operation exceeded five seconds"));
        try{active.close();}catch(IOException error){ClientErrors.report("Discord RPC close stalled transport",error);}
    }

    private void disconnect(){
        var old=channel;
        channel=null;
        session=null;
        inIo=0;
        if(old!=null)try{old.close();}catch(IOException error){ClientErrors.report("Discord RPC disconnect",error);}
    }

    private void shutdown(){
        worker.shutdown();
        watchdog.shutdown();
    }

    @Override public void close(){
        synchronized(lock){
            if(closed)return;
            closed=true;
            desired=null;
            if(!running)shutdown();
        }
    }
}
