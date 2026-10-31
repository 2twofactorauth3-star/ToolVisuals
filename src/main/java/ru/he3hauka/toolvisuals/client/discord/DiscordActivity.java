package ru.he3hauka.toolvisuals.client.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;

public record DiscordActivity(String name,String details,String state,String imageText,long started,String button,String secondaryButton,URI secondaryUrl) {
    public enum Mode { MENU,CONNECTING,SINGLEPLAYER,MULTIPLAYER,REPLAY }
    public record Context(Mode mode,String nativeVersion,String gameVersion,String clientVersion,String server){
        public Context{server=mode==Mode.MULTIPLAYER?DiscordServer.visible(server):"";}
    }

    public DiscordActivity{name=clean(name,128);if(name.isEmpty())name=DiscordBrand.NAME;details=clean(details,128);state=clean(state,128);imageText=clean(imageText,128);button=clean(button,32);secondaryButton=clean(secondaryButton,32);if(secondaryButton.isEmpty())secondaryUrl=null;else if(secondaryUrl==null)throw new IllegalArgumentException("Missing secondary button URL");if(started<0)throw new IllegalArgumentException("Invalid activity timestamp");}
    public static DiscordActivity create(Context context,UiLanguage language,long started){
        String details=UiI18n.text(language,"toolvisuals.discord.mode."+context.mode.name().toLowerCase(java.util.Locale.ROOT));
        if(!context.server.isEmpty())details=UiI18n.text(language,"toolvisuals.discord.server",context.server);
        String state=UiI18n.text(language,"toolvisuals.discord.version",clean(context.gameVersion,32));
        String name=(DiscordBrand.NAME+" "+clean(context.nativeVersion,32)).strip();
        boolean multiplayer=context.mode==Mode.MULTIPLAYER,toolrise=!context.server.isEmpty();
        String secondaryButton=multiplayer?UiI18n.text(language,"toolvisuals.discord."+(toolrise?"join":"developer")):"";
        URI secondaryUrl=multiplayer?(toolrise?DiscordBrand.JOIN_TOOLRISE:DiscordBrand.DEVELOPER):null;
        return new DiscordActivity(name,details,state,name+" · "+clean(context.clientVersion,24),started,UiI18n.text(language,"toolvisuals.discord.download"),secondaryButton,secondaryUrl);
    }
    public static String resolveVersion(String nativeVersion,java.util.function.Supplier<String> protocol,
                                        java.util.function.Consumer<Throwable> failure) {
        try {
            String version=clean(protocol.get(),32);
            return version.isEmpty()||version.equalsIgnoreCase("unknown")?nativeVersion:version;
        } catch(RuntimeException|LinkageError error) {
            failure.accept(error);
            return nativeVersion;
        }
    }
    public JsonObject json(){
        var activity=new JsonObject();activity.addProperty("name",name);activity.addProperty("type",0);activity.addProperty("details",details);activity.addProperty("state",state);
        var assets=new JsonObject();assets.addProperty("large_image",DiscordBrand.LARGE_IMAGE);assets.addProperty("large_text",imageText);activity.add("assets",assets);
        if(started>0){var timestamps=new JsonObject();timestamps.addProperty("start",started);activity.add("timestamps",timestamps);}
        var buttons=new JsonArray();addButton(buttons,button,DiscordBrand.DOWNLOAD);
        if(!secondaryButton.isEmpty())addButton(buttons,secondaryButton,secondaryUrl);
        activity.add("buttons",buttons);return activity;
    }
    private static void addButton(JsonArray buttons,String label,URI url){
        var button=new JsonObject();button.addProperty("label",label);button.addProperty("url",url.toString());buttons.add(button);
    }
    private static String clean(String value,int limit){
        if(value==null)return "";var text=new StringBuilder();int bytes=0;
        for(int index=0;index<value.length();){int cp=value.codePointAt(index);index+=Character.charCount(cp);
            if(Character.isISOControl(cp)||Character.getType(cp)==Character.FORMAT||cp>=0xd800&&cp<=0xdfff)continue;
            int count=cp<0x80?1:cp<0x800?2:cp<0x10000?3:4;if(bytes+count>limit)break;text.appendCodePoint(cp);bytes+=count;
        }return text.toString().strip();
    }
}
