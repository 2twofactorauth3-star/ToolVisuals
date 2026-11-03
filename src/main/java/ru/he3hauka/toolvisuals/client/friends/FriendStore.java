package ru.he3hauka.toolvisuals.client.friends;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.connection.ConnectionIdentity;

public final class FriendStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final int LIMIT = 256;
    public enum ArmorStyle { OVERLAY,FILL }
    public enum Option { ARMOR,ARMOR_STYLE,UNARMORED,NAME_COLOR,FRIEND_LABEL,GLOBAL,JOIN,LEAVE }
    public static final class Friend {
        public final String name;
        public final int color;
        public final ColorStyle style;
        public final FriendDisplay display;
        public Friend(String name,int color) { this(name,ColorStyle.solid(color)); }
        public Friend(String name,ColorStyle style) { this(name,style,FriendDisplay.DEFAULT); }
        public Friend(String name,ColorStyle style,FriendDisplay display) { this.name=name;this.style=ColorStyle.safe(style,0x77caff);this.color=this.style.colors.getFirst();this.display=display==null?FriendDisplay.DEFAULT:display; }
    }
    public static final class Settings {
        public final boolean armor, global, join, leave, unarmored, nameColor, friendLabel;
        public final ArmorStyle armorStyle;
        public final int color;
        public final ColorStyle style;
        public Settings(boolean armor,boolean global,int color,boolean join,boolean leave) {
            this(armor,global,ColorStyle.solid(color),join,leave);
        }
        public Settings(boolean armor,boolean global,ColorStyle style,boolean join,boolean leave) {
            this(armor,global,style,join,leave,true);
        }
        public Settings(boolean armor,boolean global,int color,boolean join,boolean leave,boolean unarmored) {
            this(armor,global,ColorStyle.solid(color),join,leave,unarmored);
        }
        public Settings(boolean armor,boolean global,ColorStyle style,boolean join,boolean leave,boolean unarmored) {
            this(armor,global,style,join,leave,unarmored,ArmorStyle.FILL,false,true);
        }
        public Settings(boolean armor,boolean global,ColorStyle style,boolean join,boolean leave,boolean unarmored,
                ArmorStyle armorStyle,boolean nameColor,boolean friendLabel) {
            this.armor=armor;this.global=global;this.style=ColorStyle.safe(style,0x77caff);this.color=this.style.colors.getFirst();
            this.join=join;this.leave=leave;this.unarmored=unarmored;
            this.armorStyle=armorStyle==null?ArmorStyle.FILL:armorStyle;this.nameColor=nameColor;this.friendLabel=friendLabel;
        }
        public Settings withStyle(ColorStyle style) {
            return new Settings(armor,global,style,join,leave,unarmored,armorStyle,nameColor,friendLabel);
        }
        public Settings toggle(Option option) {
            return new Settings(option==Option.ARMOR?!armor:armor,option==Option.GLOBAL?!global:global,style,
                    option==Option.JOIN?!join:join,option==Option.LEAVE?!leave:leave,option==Option.UNARMORED?!unarmored:unarmored,
                    option==Option.ARMOR_STYLE?(armorStyle==ArmorStyle.FILL?ArmorStyle.OVERLAY:ArmorStyle.FILL):armorStyle,
                    option==Option.NAME_COLOR?!nameColor:nameColor,option==Option.FRIEND_LABEL?!friendLabel:friendLabel);
        }
    }
    private final ru.he3hauka.toolvisuals.client.storage.JsonFile file;
    private volatile Map<String,Friend> friends = Map.of();
    private volatile Settings settings = new Settings(true,true,0x77caff,true,true);
    
    private volatile long revision;
    public FriendStore(Path path) { file=new ru.he3hauka.toolvisuals.client.storage.JsonFile(path,1024*1024); }
    public long revision() { return revision; }
    public List<Friend> entries() { return friends.values().stream().sorted(Comparator.comparing(f->f.name.toLowerCase(Locale.ROOT))).toList(); }
    public Settings settings() { return settings; }
    public Friend get(String name) { return name == null ? null : friends.get(name.toLowerCase(Locale.ROOT)); }
    public int armorTint(String name) {
        var options=settings; var friend=get(name);
        return !options.armor || friend == null ? 0 : (options.armorStyle==ArmorStyle.FILL?0xff000000:0xcc000000) | color(friend,options);
    }
    public int bodyTint(String name,boolean hasArmor) {
        var options=settings;var friend=get(name);
        return hasArmor||!options.unarmored||friend==null?0:0xff000000|color(friend,options);
    }
    public int color(Friend friend) { return color(friend,settings); }
    private static int color(Friend friend,Settings options) {
        return (options.global?options.style:friend.style).sample(ColorStyle.clock(),0);
    }
    public boolean add(String name) throws IOException {
        if (!ConnectionIdentity.valid(name)) throw new IllegalArgumentException("Invalid friend nickname");
        if (get(name) != null) return false;
        if (friends.size() >= LIMIT) throw new IllegalStateException("Friend limit reached");
        var next=new LinkedHashMap<>(friends);next.put(name.toLowerCase(Locale.ROOT),new Friend(name,settings.style));
        write(next,settings);friends=Map.copyOf(next);revision++;return true;
    }
    public void remove(String name) throws IOException {
        var next=new LinkedHashMap<>(friends);next.remove(name.toLowerCase(Locale.ROOT));write(next,settings);friends=Map.copyOf(next);revision++;
    }
    public void color(String name,int color) throws IOException {
        color(name,ColorStyle.solid(color));
    }
    public void color(String name,ColorStyle style) throws IOException {
        var old=get(name);if(old==null)throw new IllegalArgumentException("Unknown friend");
        var next=new LinkedHashMap<>(friends);next.put(name.toLowerCase(Locale.ROOT),new Friend(old.name,style,old.display));
        write(next,settings);friends=Map.copyOf(next);revision++;
    }
    public void configure(Settings next) throws IOException { write(friends,next);settings=next;revision++; }
    public void display(String name,FriendDisplay display) throws IOException {
        var old=get(name);if(old==null)throw new IllegalArgumentException("Unknown friend");
        var next=new LinkedHashMap<>(friends);next.put(name.toLowerCase(Locale.ROOT),new Friend(old.name,old.style,Objects.requireNonNull(display)));
        write(next,settings);friends=Map.copyOf(next);revision++;
    }
    public void load() throws IOException {
        try {
            file.read(json->{
            var document=json.getAsJsonObject();
            var data=GSON.fromJson(document,Saved.class);
            if(data==null||data.version!=1||data.friends==null||data.friends.size()>LIMIT)throw new IllegalArgumentException("Invalid friends");
            var next=new LinkedHashMap<String,Friend>();
            int rowIndex=0;
            for(var friend:data.friends) {
                if(friend==null||!ConnectionIdentity.valid(friend.name)||friend.color<0||friend.color>0xffffff)throw new IllegalArgumentException("Invalid friend");
                var row=document.getAsJsonArray("friends").get(rowIndex++).getAsJsonObject();
                if(next.putIfAbsent(friend.name.toLowerCase(Locale.ROOT),new Friend(friend.name,ColorStyle.safe(friend.style,friend.color),FriendDisplay.decode(row.get("display"))))!=null)throw new IllegalArgumentException("Duplicate friend");
            }
            if(data.settings!=null) {
                var savedSettings=document.getAsJsonObject("settings");
                boolean unarmored=!savedSettings.has("unarmored")||savedSettings.get("unarmored").isJsonNull()||data.settings.unarmored;
                boolean friendLabel=!savedSettings.has("friendLabel")||savedSettings.get("friendLabel").isJsonNull()||data.settings.friendLabel;
                settings=new Settings(data.settings.armor,data.settings.global,ColorStyle.safe(data.settings.style,data.settings.color),
                        data.settings.join,data.settings.leave,unarmored,data.settings.armorStyle,data.settings.nameColor,friendLabel);
            }
            friends=Map.copyOf(next);revision++;
            return Boolean.TRUE;
            });
        } catch(IOException|RuntimeException error) { throw new IOException("Unreadable friend configuration; original preserved",error); }
    }
    private void write(Map<String,Friend> next,Settings options) throws IOException {
        var data=new Saved();data.version=1;data.settings=options;data.friends=new ArrayList<>(next.values());
        file.write(GSON.toJson(data));
    }
    private static final class Saved { int version;List<Friend> friends;Settings settings; }
}
