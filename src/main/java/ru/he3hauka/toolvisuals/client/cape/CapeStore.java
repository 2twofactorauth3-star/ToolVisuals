package ru.he3hauka.toolvisuals.client.cape;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import com.google.gson.*;

import ru.he3hauka.toolvisuals.client.storage.JsonFile;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;
import ru.he3hauka.toolvisuals.client.cape.animation.CapeAnimationCodec;

public final class CapeStore {
    public static final int LIMIT=32;
    public record Entry(UUID id,String name,CapeDesign design){
        public Entry{Objects.requireNonNull(id);Objects.requireNonNull(design);name=validName(name);}
    }
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private final JsonFile file;
    private volatile Map<UUID,Entry> entries=Map.of();
    private volatile UUID selected;
    private volatile long revision;
    public CapeStore(Path path){file=new JsonFile(path);}
    public JsonFile.Status status(){return file.status();}
    public long revision(){return revision;}
    public List<Entry> entries(){return List.copyOf(entries.values());}
    public Entry selected(){return selected==null?null:entries.get(selected);}
    public void load() throws IOException {
        var saved=file.read(json->{
            var data=json.getAsJsonObject();int version=data.get("version").getAsInt();if(version!=1&&version!=2)throw new IllegalArgumentException("Unknown cape library version");
            var rows=data.getAsJsonArray("entries");if(rows.size()>LIMIT)throw new IllegalArgumentException("Cape library is full");
            var next=new LinkedHashMap<UUID,Entry>();
            for(var row:rows){var object=row.getAsJsonObject();var id=UUID.fromString(object.get("id").getAsString());var pixels=object.get("pixels");var design=version==1?CapeDesign.decode(Base64.getDecoder().decode(pixels.getAsString())):CapeAnimationCodec.decode(pixels);var entry=new Entry(id,object.get("name").getAsString(),design);if(next.putIfAbsent(id,entry)!=null)throw new IllegalArgumentException("Duplicate cape ID");}
            UUID active=data.has("selected")?UUID.fromString(data.get("selected").getAsString()):null;
            if(active!=null&&!next.containsKey(active))throw new IllegalArgumentException("Unknown selected cape");
            return new Snapshot(Collections.unmodifiableMap(next),active);
        });
        if(saved!=null){entries=saved.entries;selected=saved.selected;revision++;}
    }
    private record Snapshot(Map<UUID,Entry> entries,UUID selected){}
    public Entry put(UUID id,String name,CapeDesign design,boolean equip) throws IOException {
        var next=new LinkedHashMap<>(entries);if(id==null)id=UUID.randomUUID();
        if(!next.containsKey(id)&&next.size()>=LIMIT)throw new IllegalArgumentException("Cape library is full");
        var entry=new Entry(id,name,design);next.put(id,entry);commit(next,equip?id:selected);return entry;
    }
    public void select(UUID id) throws IOException {if(id!=null&&!entries.containsKey(id))throw new IllegalArgumentException("Unknown cape");commit(entries,id);}
    public void delete(UUID id) throws IOException {var next=new LinkedHashMap<>(entries);next.remove(id);commit(next,Objects.equals(selected,id)?null:selected);}
    private void commit(Map<UUID,Entry> next,UUID active) throws IOException {
        var root=new JsonObject();root.addProperty("version",2);if(active!=null)root.addProperty("selected",active.toString());
        var rows=new JsonArray();for(var entry:next.values()){var row=new JsonObject();row.addProperty("id",entry.id.toString());row.addProperty("name",entry.name);row.add("pixels",CapeAnimationCodec.encode(entry.design));rows.add(row);}root.add("entries",rows);
        file.write(JSON.toJson(root));entries=Collections.unmodifiableMap(new LinkedHashMap<>(next));selected=active;revision++;
    }
    private static String validName(String name){if(name==null)throw new IllegalArgumentException("Missing cape name");name=name.strip();if(name.isEmpty()||name.codePointCount(0,name.length())>32||name.codePoints().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Invalid cape name");return name;}
}
