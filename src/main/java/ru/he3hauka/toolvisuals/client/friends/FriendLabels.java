package ru.he3hauka.toolvisuals.client.friends;

import java.util.Locale;
import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;

public final class FriendLabels {
    private static final FriendLabelCache CACHE = new FriendLabelCache();
    private FriendLabels() {}
    public static synchronized Component name(FriendStore store,String profile,Component original) {
        if(original==null||profile==null||!store.settings().nameColor)return original;
        var cached=CACHE.get(store,profile);
        if(cached.friend==null)return original;
        int color=store.color(cached.friend);
        if(cached.nameColor==color&&cached.matches(original))return cached.name==null?original:cached.name;
        Component result=recolor(original,profile,color);
        if(cached.capture(original)) {
            cached.nameColor=color;
            cached.name=result==original?null:result;
        } else { cached.nameColor=-1;cached.name=null; }
        return result;
    }
    private static Component recolor(Component original,String profile,int color) {
        String plain=original.getString();
        int start=nameStart(plain,profile);
        if(start<0)return original;
        int end=start+profile.length();
        var result=Component.empty();int[] offset={0};
        original.visit((Style style,String text)->{
            int first=Math.clamp(start-offset[0],0,text.length()),last=Math.clamp(end-offset[0],0,text.length());
            if(first>0)result.append(Component.literal(text.substring(0,first)).setStyle(style));
            if(last>first)result.append(Component.literal(text.substring(first,last)).setStyle(style.withColor(color)));
            if(last<text.length())result.append(Component.literal(text.substring(last)).setStyle(style));
            offset[0]+=text.length();return Optional.empty();
        },Style.EMPTY);
        return result;
    }
    private static int nameStart(String display,String profile) {
        String text=display.toLowerCase(Locale.ROOT),name=profile.toLowerCase(Locale.ROOT);
        int start=text.indexOf(name);
        while(start>=0) {
            int end=start+name.length();
            if((start==0||!nameCharacter(text.charAt(start-1)))&&(end==text.length()||!nameCharacter(text.charAt(end))))return start;
            start=text.indexOf(name,start+1);
        }
        return -1;
    }
    private static boolean nameCharacter(char c) { return c>='a'&&c<='z'||c>='0'&&c<='9'||c=='_'; }
    public static synchronized Component badge(FriendStore store,String profile,Component visibleName,UiLanguage language) {
        if(visibleName==null||profile==null||!store.settings().friendLabel)return null;
        var cached=CACHE.get(store,profile);
        if(cached.friend==null)return null;
        int color=store.color(cached.friend);
        if(cached.badge==null||cached.badgeColor!=color||cached.badgeLanguage!=language) {
            cached.badge=Component.literal(UiI18n.text(language,"toolvisuals.friends.badge")).withColor(color);
            cached.badgeColor=color;
            cached.badgeLanguage=language;
        }
        return cached.badge;
    }
}
