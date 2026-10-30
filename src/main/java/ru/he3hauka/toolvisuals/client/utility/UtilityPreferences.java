package ru.he3hauka.toolvisuals.client.utility;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ru.he3hauka.toolvisuals.client.storage.JsonFile;

public final class UtilityPreferences {
    public static final int MAX_SETS=64, MAX_PACKS=256, MAX_FAVORITES=512;
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private record Snapshot(List<String> filters,List<String> hiddenNames,Set<String> favorites,Map<String,List<String>> packSets) {
        Snapshot {
            filters=List.copyOf(filters);hiddenNames=List.copyOf(hiddenNames);favorites=Set.copyOf(favorites);
            if(favorites.size()>MAX_FAVORITES||packSets.size()>MAX_SETS)throw new IllegalArgumentException("Too many saved entries");
            var sets=new LinkedHashMap<String,List<String>>();
            packSets.forEach((name,ids)->{
                if(name==null||name.isBlank()||name.length()>48||ids==null)throw new IllegalArgumentException("Invalid resource pack set");
                var packs=List.copyOf(new LinkedHashSet<>(ids));
                if(packs.size()>MAX_PACKS||packs.stream().anyMatch(id->id==null||id.isBlank()))throw new IllegalArgumentException("Too many or invalid resource packs");
                sets.put(name,packs);
            });
            packSets=Collections.unmodifiableMap(sets);
        }
    }
    private final JsonFile file;
    private volatile Snapshot state=new Snapshot(List.of("trade","торг"),List.of(),Set.of(),Map.of());
    public UtilityPreferences(Path path){file=new JsonFile(path);}
    public JsonFile.Status status(){return file.status();}
    public List<String> filters(){return state.filters;}
    public List<String> hiddenNames(){return state.hiddenNames;}
    public Map<String,List<String>> packSets(){return state.packSets;}
    public boolean favorite(String id){return state.favorites.contains(id);}
    public void toggleFavorite(String id) throws IOException {
        Objects.requireNonNull(id);var current=state;var favorites=new LinkedHashSet<>(current.favorites);
        if(!favorites.remove(id))favorites.add(id);
        update(new Snapshot(current.filters,current.hiddenNames,favorites,current.packSets));
    }
    public void setFilters(String text) throws IOException {var current=state;update(new Snapshot(terms(text,32,80),current.hiddenNames,current.favorites,current.packSets));}
    public void setHiddenNames(String text) throws IOException {
        var current=state;var names=terms(text,128,16).stream().filter(name->name.matches("[A-Za-z0-9_]{1,16}")).toList();
        update(new Snapshot(current.filters,names,current.favorites,current.packSets));
    }
    public void storePacks(String name,Collection<String> ids) throws IOException {
        if(name==null||name.strip().isEmpty()||name.strip().length()>48)throw new IllegalArgumentException("Invalid resource pack set name");
        var current=state;var sets=new LinkedHashMap<>(current.packSets);sets.put(name.strip(),List.copyOf(ids));
        update(new Snapshot(current.filters,current.hiddenNames,current.favorites,sets));
    }
    public void removePacks(String name) throws IOException {
        var current=state;var sets=new LinkedHashMap<>(current.packSets);sets.remove(name);
        update(new Snapshot(current.filters,current.hiddenNames,current.favorites,sets));
    }
    public static List<String> terms(String text,int count,int length) {
        if(text==null)return List.of();
        return Arrays.stream(text.split("[,;\\n]")).map(String::strip).filter(s->!s.isEmpty()).map(s->s.substring(0,Math.min(length,s.length()))).distinct().limit(count).toList();
    }
    public void load() {
        try {
            var loaded=file.read(json->{
                var saved=JSON.fromJson(json,Saved.class);if(saved==null)throw new IOException("Missing utility preferences");
                return new Snapshot(saved.filters==null?state.filters:terms(String.join(",",saved.filters),32,80),
                        saved.hiddenNames==null?List.of():terms(String.join(",",saved.hiddenNames),128,16).stream().filter(name->name.matches("[A-Za-z0-9_]{1,16}")).toList(),
                        saved.favorites==null?Set.of():saved.favorites,saved.packSets==null?Map.of():saved.packSets);
            });
            if(loaded!=null)state=loaded;
        }catch(IOException error){org.slf4j.LoggerFactory.getLogger("ToolVisuals").warn("Cannot load utility preferences; original file preserved",error);}
    }
    public void save() throws IOException {file.write(JSON.toJson(state));}
    private void update(Snapshot next) throws IOException {file.write(JSON.toJson(next));state=next;}
    private static final class Saved {List<String> filters,hiddenNames;Set<String> favorites;Map<String,List<String>> packSets;}
}
