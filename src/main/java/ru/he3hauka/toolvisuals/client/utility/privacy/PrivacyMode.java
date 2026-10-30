package ru.he3hauka.toolvisuals.client.utility.privacy;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.nameprotect.NameMask;
import ru.he3hauka.toolvisuals.client.optimization.cache.BoundedCache;
import ru.he3hauka.toolvisuals.client.optimization.text.StableText;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class PrivacyMode {
    private static List<NameMask> masks=List.of();
    private static String signature="";
    private static long revision;
    public static long revision(){return revision;}
    private static final BoundedCache<StableText,FormattedCharSequence> cached=new BoundedCache<>(256);
    private PrivacyMode(){}
    public static boolean active(){return ToolVisualsClient.CONFIG.active(STREAMER_MODE);}
    public static boolean coordinates(){return active()&&ToolVisualsClient.CONFIG.flag(STREAMER_COORDINATES);}
    public static void tick(Minecraft client) {
        var config=ToolVisualsClient.CONFIG;var replacements=new LinkedHashMap<String,String>();
        if(active()) {
            if(config.flag(STREAMER_SELF)&&client.getUser()!=null)replacements.put(client.getUser().getName(),"ToolVisuals");
            if(config.flag(STREAMER_FRIENDS))for(var friend:ToolVisualsClient.FRIENDS.entries())replacements.put(friend.name,"HiddenPlayer");
            for(String name:ToolVisualsClient.UTILITIES.hiddenNames())replacements.put(name,"HiddenPlayer");
            if(config.flag(STREAMER_SERVER)&&client.getCurrentServer()!=null){String ip=client.getCurrentServer().ip;replacements.put(ip,"HiddenServer");int colon=ip.lastIndexOf(':');if(colon>0&&ip.indexOf(':')==colon)replacements.put(ip.substring(0,colon),"HiddenServer");}
        }
        String next=replacements.toString();if(next.equals(signature))return;signature=next;revision++;
        masks=replacements.entrySet().stream().filter(entry->!entry.getKey().isBlank()).map(entry->new NameMask(entry.getKey(),entry.getValue())).toList();cached.clear();
        client.gui.hud.getChat().rescaleChat();
    }
    public static String text(String text){if(!active())return text;for(var mask:masks)text=mask.apply(text);return text;}
    public static FormattedText text(FormattedText text){if(!active()||masks.isEmpty())return text;String plain=text.getString();for(var mask:masks){String next=mask.apply(plain);if(!plain.equals(next)){text=mask.apply(text);plain=next;}}return text;}
    public static FormattedCharSequence text(FormattedCharSequence text) {
        if(!active()||masks.isEmpty())return text;
        StableText key=text instanceof StableText stable?stable:null;var previous=key==null?null:cached.get(key);if(previous!=null)return previous;
        var builder=new StringBuilder();text.accept((index,style,cp)->{builder.appendCodePoint(cp);return true;});String plain=builder.toString();
        var result=text;
        for(var mask:masks){String next=mask.apply(plain);if(!plain.equals(next)){result=mask.apply(result);plain=next;}}
        if(key!=null){if(result!=text)result=new StableText(result);cached.put(key,result);}return result;
    }
}
