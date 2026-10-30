package ru.he3hauka.toolvisuals.client.cosmetic.outfit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import com.google.gson.*;
import ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryConfigCodec;
import ru.he3hauka.toolvisuals.client.storage.JsonFile;
import ru.he3hauka.toolvisuals.client.cape.animation.CapeAnimationCodec;
import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

public final class OutfitStore {
    public static final int LIMIT=16;
    public record Entry(UUID id,String name,AccessoryLook look,CapeDesign cape){
        public Entry{Objects.requireNonNull(id);Objects.requireNonNull(look);name=validName(name);}
    }
    private final JsonFile file;
    private volatile Map<UUID,Entry> entries=Map.of();
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();

    public OutfitStore(Path path){file=new JsonFile(path);}
    public List<Entry> entries(){return List.copyOf(entries.values());}
    public JsonFile.Status status(){return file.status();}
    public void load() throws IOException {
        var saved=file.read(json->{
            var root=json.getAsJsonObject();if(root.get("version").getAsInt()!=1)throw new IllegalArgumentException("Unknown outfit version");
            var rows=root.getAsJsonArray("entries");if(rows.size()>LIMIT)throw new IllegalArgumentException("Too many outfits");
            var next=new LinkedHashMap<UUID,Entry>();
            for(var row:rows){var value=row.getAsJsonObject();var id=UUID.fromString(value.get("id").getAsString());CapeDesign cape=null;
                if(value.has("cape"))cape=CapeAnimationCodec.decode(value.get("cape"));
                var entry=new Entry(id,value.get("name").getAsString(),AccessoryConfigCodec.decode(value.get("look").getAsString()),cape);
                if(next.putIfAbsent(id,entry)!=null)throw new IllegalArgumentException("Duplicate outfit ID");
            }validateNames(next);return Collections.unmodifiableMap(next);
        });if(saved!=null)entries=saved;
    }
    public Entry put(UUID id,String name,AccessoryLook look,CapeDesign cape) throws IOException {
        var next=new LinkedHashMap<>(entries);if(id==null)id=UUID.randomUUID();if(!next.containsKey(id)&&next.size()>=LIMIT)throw new IllegalArgumentException("Outfit library is full");
        var entry=new Entry(id,name,look,cape);next.put(id,entry);commit(next);return entry;
    }
    public void delete(UUID id) throws IOException {var next=new LinkedHashMap<>(entries);next.remove(id);commit(next);}
    private void commit(Map<UUID,Entry> next) throws IOException {
        validateNames(next);var root=new JsonObject();root.addProperty("version",1);var rows=new JsonArray();
        for(var entry:next.values()){var row=new JsonObject();row.addProperty("id",entry.id.toString());row.addProperty("name",entry.name);row.addProperty("look",AccessoryConfigCodec.encode(entry.look));if(entry.cape!=null)row.add("cape",CapeAnimationCodec.encode(entry.cape));rows.add(row);}root.add("entries",rows);
        file.write(JSON.toJson(root));entries=Collections.unmodifiableMap(new LinkedHashMap<>(next));
    }
    private static void validateNames(Map<UUID,Entry> values){var names=new HashSet<String>();for(var entry:values.values())if(!names.add(entry.name.toLowerCase(Locale.ROOT)))throw new IllegalArgumentException("Duplicate outfit name");}
    private static String validName(String name){if(name==null)throw new IllegalArgumentException("Missing outfit name");name=name.strip();if(name.isEmpty()||name.codePointCount(0,name.length())>32||name.codePoints().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Invalid outfit name");return name;}
}
