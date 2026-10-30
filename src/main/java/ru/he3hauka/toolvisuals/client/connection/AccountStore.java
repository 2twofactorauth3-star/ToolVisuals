package ru.he3hauka.toolvisuals.client.connection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.LoggerFactory;

import ru.he3hauka.toolvisuals.client.storage.JsonFile;

public final class AccountStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final int LIMIT = 128;
    public static final class Profile {
        public final String name;
        public final boolean favorite;
        public final long lastUsed;
        Profile(String name, boolean favorite, long lastUsed) { this.name=name; this.favorite=favorite; this.lastUsed=lastUsed; }
    }
    private final JsonFile file;
    private volatile List<Profile> profiles = List.of();
    public AccountStore(Path path) { file=new JsonFile(path); }
    public JsonFile.Status status() { return file.status(); }
    public List<Profile> profiles() { return profiles; }
    public boolean add(String name) throws IOException {
        if (!ConnectionIdentity.valid(name)) throw new IllegalArgumentException("Invalid nickname");
        if (profiles.stream().anyMatch(p->p.name.equals(name))) return false;
        if (profiles.size()>=LIMIT) throw new IllegalStateException("Account list is full");
        var next=new ArrayList<>(profiles); next.add(new Profile(name,false,0)); update(next); return true;
    }
    public void used(String name) throws IOException {
        if (!ConnectionIdentity.valid(name)) throw new IllegalArgumentException("Invalid nickname");
        var next=new ArrayList<>(profiles);
        var old=next.stream().filter(p->p.name.equals(name)).findFirst().orElse(null);
        if (old==null && next.size()>=LIMIT) return;
        next.removeIf(p->p.name.equals(name)); next.add(new Profile(name,old!=null&&old.favorite,System.currentTimeMillis())); update(next);
    }
    public void favorite(String name, boolean favorite) throws IOException {
        update(profiles.stream().map(p->p.name.equals(name)?new Profile(p.name,favorite,p.lastUsed):p).toList());
    }
    public void remove(String name) throws IOException { update(profiles.stream().filter(p->!p.name.equals(name)).toList()); }
    private void update(List<Profile> next) throws IOException {
        var sorted=sort(next);
        var data=new Data(); data.profiles=new ArrayList<>();
        for(var profile:sorted){var entry=new SavedProfile();entry.name=profile.name;entry.favorite=profile.favorite;entry.lastUsed=profile.lastUsed;data.profiles.add(entry);}
        file.write(GSON.toJson(data));
        profiles=sorted;
    }
    private static List<Profile> sort(List<Profile> profiles) {
        return profiles.stream().sorted(Comparator.comparing((Profile p)->!p.favorite)
                .thenComparing(Comparator.comparingLong((Profile p)->p.lastUsed).reversed())
                .thenComparing(p->p.name,String.CASE_INSENSITIVE_ORDER)).toList();
    }
    public void load() {
        try {
            var loaded=file.read(json->{
            var data=GSON.fromJson(json,Data.class);
            if (data==null||data.profiles==null) throw new IOException("Missing account list");
            var next=new ArrayList<Profile>();
            for (var item:data.profiles) if (item!=null&&ConnectionIdentity.valid(item.name)
                    &&next.size()<LIMIT&&next.stream().noneMatch(p->p.name.equals(item.name)))
                next.add(new Profile(item.name,item.favorite,Math.max(0,item.lastUsed)));
            return sort(next);
            });
            if(loaded!=null)profiles=loaded;
        } catch (IOException error) { LoggerFactory.getLogger("ToolVisuals").warn("Cannot read saved nicknames from {}",file.path,error); }
    }
    private static final class Data { public List<SavedProfile> profiles; }
    private static final class SavedProfile { public String name; public boolean favorite; public long lastUsed; }
}
