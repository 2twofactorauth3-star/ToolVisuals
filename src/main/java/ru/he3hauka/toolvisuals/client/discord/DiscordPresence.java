package ru.he3hauka.toolvisuals.client.discord;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.chat.message.ChatMessages;
import ru.he3hauka.toolvisuals.client.connection.ConnectionVersions;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.discord.ipc.RpcTransport;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;
import ru.he3hauka.toolvisuals.client.replay.ui.ReplayViewerScreen;

public final class DiscordPresence {
    private static final DiscordRpc RPC=new DiscordRpc(new RpcTransport.Factory()::open);
    private static final long STARTED=System.currentTimeMillis()/1000;
    private static final String VERSION=version("toolvisuals"),MINECRAFT=version("minecraft");
    private static long checkedAt=Long.MIN_VALUE;
    private static UiLanguage language;
    private static boolean initialized;

    private DiscordPresence(){}

    public static void initialize(){
        if(initialized)return;
        initialized=true;
        ClientTickEvents.END_CLIENT_TICK.register(DiscordPresence::tick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client->RPC.close());
    }

    private static String version(String id){
        return FabricLoader.getInstance().getModContainer(id).orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    private static void tick(Minecraft client){
        var selected=ToolVisualsClient.CONFIG.language();
        long now=System.nanoTime();
        if(checkedAt!=Long.MIN_VALUE&&now-checkedAt<1_000_000_000L&&selected==language)return;
        checkedAt=now;
        language=selected;
        try{
            var mode=mode(client);var server=client.getCurrentServer();
            var context=new DiscordActivity.Context(mode,MINECRAFT,gameVersion(client,mode),VERSION,server==null?"":server.ip);
            RPC.publish(DiscordActivity.create(context,language,STARTED));
        }catch(RuntimeException|LinkageError error){ClientErrors.report("Capture Discord RPC activity",error);}
    }

    private static String gameVersion(Minecraft client,DiscordActivity.Mode mode){
        if(mode==DiscordActivity.Mode.SINGLEPLAYER||mode==DiscordActivity.Mode.REPLAY)return MINECRAFT;
        var connection=client.getConnection();
        return DiscordActivity.resolveVersion(MINECRAFT,()->ConnectionVersions.label(connection==null?null:connection.getConnection()),
                error->ClientErrors.report("Resolve protocol version for Discord RPC; using native Minecraft version",error));
    }
    public static void diagnostic() {
        var status=RPC.status();
        org.slf4j.LoggerFactory.getLogger("ToolVisuals/DiscordRPC").info("RPC diagnostic: status={}, application_id={}, game_pid={}, native_minecraft={}, mod_version={}",
                status,DiscordBrand.APPLICATION_ID,ProcessHandle.current().pid(),MINECRAFT,VERSION);
        ChatMessages.notice("discord.status",status.name(),MINECRAFT);
        if(status==DiscordRpc.Status.CONNECTED)ChatMessages.notice("discord.competing");
    }

    private static DiscordActivity.Mode mode(Minecraft client){
        if(client.gui.screen() instanceof ReplayViewerScreen)return DiscordActivity.Mode.REPLAY;
        if(client.level!=null)return client.hasSingleplayerServer()?DiscordActivity.Mode.SINGLEPLAYER:DiscordActivity.Mode.MULTIPLAYER;
        if(client.gui.screen() instanceof ConnectScreen||client.getConnection()!=null)return DiscordActivity.Mode.CONNECTING;
        return DiscordActivity.Mode.MENU;
    }
}
