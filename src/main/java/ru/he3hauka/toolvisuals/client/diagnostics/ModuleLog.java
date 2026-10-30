package ru.he3hauka.toolvisuals.client.diagnostics;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class ModuleLog {
    private static final ThreadLocal<Boolean> SAMPLING=ThreadLocal.withInitial(()->false);
    private static volatile TraceSession session;
    private static Path directory;
    private static ExecutorService worker;
    private static volatile TraceSnapshot snapshot;
    private static long sampled;

    private ModuleLog(){}

    public static void initialize(){
        if(directory!=null)return;
        directory=FabricLoader.getInstance().getGameDir().resolve("logs/toolvisuals");
        ClientTickEvents.END_CLIENT_TICK.register(ModuleLog::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{
            var current=session;if(current!=null&&current.connection==handler)current.stop(TraceSession.Stop.DISCONNECTED);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->{
            var current=session;if(current!=null)current.stop(TraceSession.Stop.CLIENT_CLOSED);
            if(worker!=null)worker.shutdown();
        });
    }

    public static boolean active(){var current=session;return current!=null&&current.active();}

    public static void checked(ClientConfig config,VisualModule module,boolean enabled){
        var current=session;
        if(current!=null&&current.active()&&!SAMPLING.get()&&config==ToolVisualsClient.CONFIG)current.check(module,enabled);
    }

    public static void action(VisualModule module,String action,String detail){event(module.title,action,detail);}
    public static void hud(VisualModule module){var current=session;if(current!=null)current.hud(module);}
    public static void event(String subject,String action,String detail){var current=session;if(current!=null)current.event(subject,action,detail);}
    public static void state(VisualModule module,String channel,String detail){state(module.title,channel,detail);}
    public static void state(String subject,String channel,String detail){var current=session;if(current!=null)current.state(subject,channel,detail);}
    public static void failure(VisualModule module,String action,Throwable error){
        ClientErrors.report(module.title+" / "+action,error);
        if(active())event(module.title,action,TraceText.failure(error));
    }

    public static void command(String argument){
        if(argument.equalsIgnoreCase("start"))start();
        else if(argument.equalsIgnoreCase("stop")){
            var current=session;
            if(current==null)ChatMessages.notice("log.not_running");
            else {current.stop(TraceSession.Stop.MANUAL);ChatMessages.notice("log.stopping");}
        }else ChatMessages.notice("log.usage");
    }

    private static synchronized void start(){
        var client=Minecraft.getInstance();
        if(client.player==null||client.level==null||client.getConnection()==null){ChatMessages.notice("log.world_required");return;}
        if(session!=null){ChatMessages.notice(session.active()?"log.already_running":"log.stopping");return;}
        if(directory==null)initialize();
        snapshot=capture();sampled=System.nanoTime();
        var header=new ArrayList<String>();
        header.add("Client-only diagnostic session; CHECK means the setting was queried, not that an action succeeded.");
        header.add("Module instrumentation omits chat and command values. Runtime WARN/ERROR messages are included with bounded sanitization. File limit: 8 MiB.");
        header.add("java="+System.getProperty("java.version")+" os="+System.getProperty("os.name"));
        FabricLoader.getInstance().getAllMods().stream().sorted(Comparator.comparing(mod->mod.getMetadata().getId())).limit(256)
                .forEach(mod->header.add("mod="+mod.getMetadata().getId()+" version="+mod.getMetadata().getVersion().getFriendlyString()));
        var options=client.options;
        header.add("bindings forward="+options.keyUp.saveString()+" backward="+options.keyDown.saveString()+" sneak="+options.keyShift.saveString()+" sprint="+options.keySprint.saveString());
        header.addAll(snapshot.header());
        var privateValues=new ArrayList<String>();
        privateValues.add(client.getUser().getName());privateValues.add(client.player.getUUID().toString());
        privateValues.add(FabricLoader.getInstance().getGameDir().toString());privateValues.add(System.getProperty("user.home",""));
        var server=client.getCurrentServer();if(server!=null)privateValues.add(server.ip);
        String name="modules-"+DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(ZonedDateTime.now())+"-"+UUID.randomUUID().toString().substring(0,8)+".log";
        var current=new TraceSession(directory.resolve(name),client.getConnection(),header,new TraceText(privateValues));
        session=current;
        var appender=new TraceAppender(current);
        try{appender.attach();}catch(RuntimeException error){current.event("LogCapture","unavailable",TraceText.failure(error));}
        current.opened.thenAccept(path->client.execute(()->{
            if(session==current&&current.active()&&client.getConnection()==current.connection)ChatMessages.notice("log.started",path(path));
        }));
        current.finished.thenAccept(result->{
            try{appender.detach();}catch(RuntimeException error){ClientErrors.report("Detach module log capture",error);}
            synchronized(ModuleLog.class){if(session==current){session=null;snapshot=null;}}
            if(result.failure()!=null)org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Module diagnostic log could not be written",result.failure());
            client.execute(()->{
                if(client.getConnection()!=current.connection||client.player==null)return;
                if(result.failure()!=null)ChatMessages.notice("log.io_error",path(result.path()));
                else if(result.reason()==TraceSession.Stop.MANUAL)ChatMessages.notice("log.stopped",path(result.path()));
                else if(result.reason()==TraceSession.Stop.SIZE_LIMIT)ChatMessages.notice("log.limit",path(result.path()));
            });
        });
        if(worker==null)worker=Executors.newSingleThreadExecutor(job->{var thread=new Thread(job,"ToolVisuals module log");thread.setDaemon(true);return thread;});
        current.start(worker);
    }

    private static TraceSnapshot capture(){
        SAMPLING.set(true);
        try{return TraceSnapshot.capture(ToolVisualsClient.CONFIG);}finally{SAMPLING.remove();}
    }

    private static Component path(Path path){
        String relative="logs/toolvisuals/"+path.getFileName();
        return Component.literal(relative).withStyle(style->style.withClickEvent(new ClickEvent.CopyToClipboard(path.toAbsolutePath().toString()))
                .withHoverEvent(new HoverEvent.ShowText(UiI18n.tr("toolvisuals.log.copy_path"))));
    }

    private static void tick(Minecraft client){
        var current=session;
        if(current==null||!current.scope(client.getConnection(),client.level!=null&&client.player!=null))return;
        long now=System.nanoTime();if(now-sampled<1_000_000_000L)return;sampled=now;
        var before=snapshot;
        if(before!=null&&before.revision()!=ToolVisualsClient.CONFIG.revision()){
            var next=capture();next.changes(before,current);snapshot=next;
        }
        var screen=client.gui.screen();
        current.state("Client","context","screen="+(screen==null?"game":screen.getClass().getSimpleName())+" focused="+client.isWindowActive()
                +" paused="+client.isPaused()+" alive="+client.player.isAlive()+" sprinting="+client.player.isSprinting()+" food="+client.player.getFoodData().getFoodLevel()
                +" forward="+client.options.keyUp.isDown()+" backward="+client.options.keyDown.isDown()+" sneak="+client.options.keyShift.isDown()+" sprint_key="+client.options.keySprint.isDown());
    }
}
