package ru.he3hauka.toolvisuals.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class Keybinds {
    public static final KeybindStore STORE=new KeybindStore(FabricLoader.getInstance().getConfigDir().resolve("toolvisuals-keybinds.json"));
    private static final ActionQueue QUEUE=new ActionQueue();
    private static boolean opening;
    private Keybinds(){}
    public static void initialize(){try{STORE.load();}catch(java.io.IOException error){org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot load keybinds; original file preserved",error);}}
    public static void requestOpen(){opening=true;}
    public static void press(InputConstants.Key key){
        if(key.equals(InputConstants.UNKNOWN))return;
        var client=Minecraft.getInstance();boolean allowed=allowed(client);QUEUE.scope(client.getConnection(),allowed);if(!allowed)return;
        for(var bind:STORE.entries())if(bind.enabled()&&bind.key().equals(key.getName()))QUEUE.offer(bind);
    }
    private static boolean allowed(Minecraft client){return client.player!=null&&client.level!=null&&client.getConnection()!=null&&client.gui.screen()==null&&client.gui.overlay()==null&&client.isWindowActive()&&ToolVisualsClient.CONFIG.active(VisualModule.KEYBINDS);}
    public static void tick(Minecraft client){
        if(opening){opening=false;client.gui.setScreen(new ru.he3hauka.toolvisuals.client.keybinds.ui.KeybindsScreen(null));}
        QUEUE.scope(client.getConnection(),allowed(client));var bind=QUEUE.poll(System.nanoTime());if(bind==null)return;
        if(!STORE.entries().contains(bind)||!bind.enabled())return;
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(VisualModule.KEYBINDS,"execute","kind="+bind.kind());
        switch(bind.kind()){
            case MODULE->{for(var module:VisualModule.values())if(module.id.equals(bind.value())){ToolVisualsClient.CONFIG.setActive(module,!ToolVisualsClient.CONFIG.active(module));break;}}
            case COMMAND->{if(ru.he3hauka.toolvisuals.client.pvp.PvPSave.allowCommand(bind.value()))client.getConnection().sendCommand(ru.he3hauka.toolvisuals.client.command.CommandFix.outgoing(bind.value()));}
            case CHAT->{if(!ru.he3hauka.toolvisuals.client.command.LocalCommands.handle(bind.value()))client.getConnection().sendChat(bind.value());}
        }
    }
}
