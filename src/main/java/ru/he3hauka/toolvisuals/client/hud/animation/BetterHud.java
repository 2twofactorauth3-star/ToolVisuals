package ru.he3hauka.toolvisuals.client.hud.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.BETTER_HUD;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class BetterHud {
    private static final Motion TAB=new Motion(0), HOTBAR=new Motion(0);
    private static long lastTab,lastHotbar,message;
    private static Object level;
    private static final ru.he3hauka.toolvisuals.client.hud.animation.OpeningTransition CHAT=new ru.he3hauka.toolvisuals.client.hud.animation.OpeningTransition();
    private static final ThreadLocal<Float> CHAT_INPUT=ThreadLocal.withInitial(()->0f),CHAT_RENDER=ThreadLocal.withInitial(()->0f);
    private BetterHud() {}
    public static void tick(Minecraft client) { if(level!=client.level) { level=client.level;TAB.snap(0);CHAT.clear();lastTab=lastHotbar=message=0; } }
    private static float speed() { return ToolVisualsClient.CONFIG.number(BETTER_HUD_SPEED); }
    public static boolean tabVisible(boolean held) {
        long now=System.nanoTime();
        if(!ToolVisualsClient.CONFIG.active(BETTER_HUD)||!ToolVisualsClient.CONFIG.flag(BETTER_HUD_TAB)) { TAB.snap(held?1:0);lastTab=now;return held; }
        TAB.step(held?1:0,lastTab==0?1/60f:Math.clamp((now-lastTab)/1e9f,0,.1f),speed());lastTab=now;
        return held||TAB.value()>.01f;
    }
    public static int tabOffset() { return ToolVisualsClient.CONFIG.active(BETTER_HUD)&&ToolVisualsClient.CONFIG.flag(BETTER_HUD_TAB)?-Math.round((1-TAB.value())*18):0; }
    public static float tabProgress() { return ToolVisualsClient.CONFIG.active(BETTER_HUD)&&ToolVisualsClient.CONFIG.flag(BETTER_HUD_TAB)?TAB.value():1; }
    public static void messageAdded() { message=System.nanoTime(); }
    public static void chatOpened(Object screen){CHAT.open(screen,System.nanoTime());}
    public static float chatOpeningOffset(){
        if(!ToolVisualsClient.CONFIG.active(BETTER_HUD)||!ToolVisualsClient.CONFIG.flag(BETTER_HUD_CHAT))return 0;
        return 18*(1-CHAT.progress(System.nanoTime(),4/Math.max(1,speed())));
    }
    public static float chatInputOffset(){return CHAT_INPUT.get();}
    public static void chatInputOffset(float value){CHAT_INPUT.set(value);}
    public static float chatRenderOffset(){return CHAT_RENDER.get();}
    public static void chatRenderOffset(float value){CHAT_RENDER.set(value);}
    public static net.minecraft.client.input.MouseButtonEvent animatedChatInput(net.minecraft.client.input.MouseButtonEvent event){
        return offsetChatInput(event,-chatOpeningOffset());
    }
    public static net.minecraft.client.input.MouseButtonEvent originalChatInput(net.minecraft.client.input.MouseButtonEvent event){
        return offsetChatInput(event,chatInputOffset());
    }
    private static net.minecraft.client.input.MouseButtonEvent offsetChatInput(net.minecraft.client.input.MouseButtonEvent event,float offset){
        return offset==0?event:new net.minecraft.client.input.MouseButtonEvent(event.x(),event.y()+offset,event.buttonInfo());
    }
    public static int chatOffset() {
        if(!ToolVisualsClient.CONFIG.active(BETTER_HUD)||!ToolVisualsClient.CONFIG.flag(BETTER_HUD_CHAT)||message==0
                ||Minecraft.getInstance().gui.screen() instanceof ChatScreen)return 0;
        return Math.round(10*(float)Math.exp(-(System.nanoTime()-message)/1e9*speed()));
    }
    public static int hotbarX(int target) {
        long now=System.nanoTime();
        if(lastHotbar==0||!ToolVisualsClient.CONFIG.active(BETTER_HUD)||!ToolVisualsClient.CONFIG.flag(BETTER_HUD_HOTBAR))HOTBAR.snap(target);
        else HOTBAR.step(target,Math.clamp((now-lastHotbar)/1e9f,0,.1f),speed());
        lastHotbar=now;return Math.round(HOTBAR.value());
    }
}
