package ru.he3hauka.toolvisuals.client.auction;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.random.RandomGenerator;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class AuctionHelper {
    private static final AuctionRunner RUNNER = new AuctionRunner(new MinecraftEnvironment(),RandomGenerator.getDefault());
    private static AuctionRecording recording;
    private static Object recordConnection;
    private static boolean internal;
    private AuctionHelper() {}
    public static void initialize() {
        try { ToolVisualsClient.AUCTIONS.load(); }
        catch(IOException error) { org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot read auction bindings; original file preserved",error); }
        ClientTickEvents.END_CLIENT_TICK.register(AuctionHelper::tick);
        ClientReceiveMessageEvents.GAME.register((message,overlay) -> { if(!overlay)RUNNER.message(message.getString()); });
    }
    public static String server() {
        var client=Minecraft.getInstance(); var server=client.getCurrentServer();
        return client.getConnection()==null || client.isLocalServer() || server==null ? "" : AuctionPlan.server(server.ip);
    }
    public static void tick(Minecraft client) {
        if(!ToolVisualsClient.CONFIG.available(VisualModule.AUCTION_HELPER)){
            recording=null;recordConnection=null;RUNNER.tick(null,false,null,false);return;
        }
        boolean enabled=ToolVisualsClient.CONFIG.active(VisualModule.AUCTION_HELPER);
        if(!enabled&&recording==null){RUNNER.tick(null,false,null,false);return;}
        String server=server();
        if(recording!=null && (recordConnection!=client.getConnection() || !recording.base.server.equals(server))) cancelRecording("record_disconnected");
        var plan=ToolVisualsClient.AUCTIONS.active(server);
        RUNNER.tick(plan,enabled,server.isEmpty()?null:client.getConnection(),recording!=null);
    }
    public static boolean recording() { return recording!=null; }
    public static int recordedClicks() { return recording==null?0:recording.clicks(); }
    public static String status() { return recording!=null?"recording":RUNNER.status(); }
    public static long remainingSeconds() { return RUNNER.remainingSeconds(); }
    public static void startRecording(AuctionPlan base) {
        if(!ToolVisualsClient.CONFIG.available(VisualModule.AUCTION_HELPER))return;
        String server=server();
        if(server.isEmpty() || !server.equals(base.server)) {notice("wrong_server");return;}
        RUNNER.interrupt(); recording=new AuctionRecording(base); recordConnection=Minecraft.getInstance().getConnection();
        notice("record_start"); notice("record_instructions"); notice("record_finish_hint");
    }
    public static void cancelRecording(String reason) { if(recording==null)return;recording=null;recordConnection=null;notice(reason); }
    public static boolean runNow() {
        tick(Minecraft.getInstance());
        return !recording() && ToolVisualsClient.CONFIG.active(VisualModule.AUCTION_HELPER) && RUNNER.requestNow();
    }
     
    public static boolean localChat(String text) {
        String line=text.strip();
        if(!line.toLowerCase(Locale.ROOT).matches("^\\.toolvisuals(?:\\s.*)?$"))return false;
        String[] tokens=line.split("\\s+",3);
        if(tokens.length<2 || !tokens[1].equalsIgnoreCase("auction")){notice("usage");return true;}
        if(!ToolVisualsClient.CONFIG.available(VisualModule.AUCTION_HELPER))return true;
        String id=tokens.length==3?tokens[2].strip():"";
        if(id.equalsIgnoreCase("cancel")){cancelRecording("record_cancelled");return true;}
        if(id.toLowerCase(Locale.ROOT).startsWith("use ")) {
            var plan=ToolVisualsClient.AUCTIONS.get(id.substring(4).strip());
            if(plan==null || !plan.server.equals(server()))notice("wrong_server");
            else try {ToolVisualsClient.AUCTIONS.put(plan,true);notice("selected",plan.id);} catch(IOException error){notice("io_error");}
            return true;
        }
        if(recording==null){notice("not_recording");return true;}
        if(recordConnection!=Minecraft.getInstance().getConnection() || !recording.base.server.equals(server())){cancelRecording("record_disconnected");return true;}
        try {
            String target=id.isEmpty()?server():id;
            var existing=ToolVisualsClient.AUCTIONS.get(target);
            if(existing!=null&&!existing.server.equals(server())) {notice("id_other_server");return true;}
            var saved=recording.finish(target);
            if(existing!=null)saved=existing.withRecording(target,saved.manual);
            ToolVisualsClient.AUCTIONS.put(saved,true);
            recording=null;recordConnection=null;notice("record_saved",saved.id,saved.manual.size()-1);
        } catch(IOException error){notice("io_error");}
        catch(RuntimeException error){notice("record_incomplete");}
        return true;
    }
    public static void outgoingCommand(String command) {
        if(internal)return;
        RUNNER.interrupt();
        if(recording==null)return;
        if(recordConnection!=Minecraft.getInstance().getConnection() || !recording.base.server.equals(server())){cancelRecording("record_disconnected");return;}
        try {recording.command(command,now());notice("record_command",command);}
        catch(RuntimeException error){ClientErrors.report("Record auction command",error);cancelRecording("record_bad_action");}
    }
    public static void playerClick(int container, int slot, int button, ContainerInput input) {
        if(internal)return;
        RUNNER.interrupt(); if(recording==null)return;
        var client=Minecraft.getInstance();
        if(recordConnection!=client.getConnection() || !recording.base.server.equals(server())){cancelRecording("record_disconnected");return;}
        var view=snapshot(client);
        if(input!=ContainerInput.PICKUP || button<0 || button>1 || view==null || view.id!=container || !view.emptyCursor || !view.buttons.containsKey(slot)) {
            cancelRecording("record_bad_action");return;
        }
        try {recording.click(view.key,view.buttons.get(slot),slot,button,now());notice("record_click",recording.clicks(),slot+1);}
        catch(IllegalStateException error){notice("record_need_command");}
        catch(RuntimeException error){ClientErrors.report("Record auction menu click",error);cancelRecording("record_bad_action");}
    }
    public static void serverMenuResponse(int container, boolean opened) { if(!ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active())RUNNER.serverMenuResponse(container,opened); }
    public static AuctionRunner.MenuView snapshot(Minecraft client) {
        try { return readSnapshot(client); } catch(RuntimeException unsupportedMenu) { ClientErrors.report("Read auction menu snapshot",unsupportedMenu);return null; }
    }
    private static AuctionRunner.MenuView readSnapshot(Minecraft client) {
        if(client.player==null || !client.player.isAlive() || !(client.gui.screen() instanceof AbstractContainerScreen<?> screen))return null;
        var menu=screen.getMenu(); if(menu.containerId<=0 || menu!=client.player.containerMenu)return null;
        var key=new AuctionPlan.MenuKey(BuiltInRegistries.MENU.getKey(menu.getType()).toString(),screen.getTitle().getString(),menu.slots.size());
        var buttons=new HashMap<Integer,AuctionPlan.ButtonKey>();
        for(int i=0;i<menu.slots.size();i++) {
            var slot=menu.slots.get(i); var item=slot.getItem();
            if(slot.container!=client.player.getInventory() && !item.isEmpty())buttons.put(i,new AuctionPlan.ButtonKey(
                    BuiltInRegistries.ITEM.getKey(item.getItem()).toString(),item.getHoverName().getString()));
        }
        return new AuctionRunner.MenuView(menu.containerId,key,buttons,menu.getCarried().isEmpty());
    }
    private static long now() { return System.nanoTime()/1_000_000L; }
    public static void notice(String key,Object... args) {
        ru.he3hauka.toolvisuals.client.chat.message.ChatMessages.notice("auction."+key,args);
    }
    private static final class MinecraftEnvironment implements AuctionRunner.Environment {
        public long now() { return AuctionHelper.now(); }
        public boolean idle() { var c=Minecraft.getInstance();return c.player!=null && c.player.isAlive() && c.gameMode!=null && c.gui.screen()==null && c.player.containerMenu==c.player.inventoryMenu && c.player.containerMenu.getCarried().isEmpty(); }
        public AuctionRunner.MenuView menu() {return snapshot(Minecraft.getInstance());}
        public void command(String command) {internal=true;try{Minecraft.getInstance().getConnection().sendCommand(command);}finally{internal=false;}}
        public void click(int container,int slot,int button) {internal=true;try{var c=Minecraft.getInstance();c.gameMode.handleContainerInput(container,slot,button,ContainerInput.PICKUP,c.player);}finally{internal=false;}}
        public void close(int container) {var c=Minecraft.getInstance();if(c.player!=null&&c.player.containerMenu.containerId==container)c.player.closeContainer();}
        public void notice(String key,Object... args) {AuctionHelper.notice(key,args);}
    }
}
