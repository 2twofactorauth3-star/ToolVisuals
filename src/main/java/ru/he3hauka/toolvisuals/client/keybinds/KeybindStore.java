package ru.he3hauka.toolvisuals.client.keybinds;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import com.google.gson.*;

import ru.he3hauka.toolvisuals.client.storage.JsonFile;

public final class KeybindStore {
    public static final int LIMIT=64;
    private final JsonFile file;
    private List<ActionBind> entries=List.of();
    public KeybindStore(Path path){file=new JsonFile(path,128*1024);}
    public List<ActionBind> entries(){return entries;}
    public JsonFile.Status status(){return file.status();}
    public void load() throws IOException {
        var loaded=file.read(json->{
            var root=json.getAsJsonObject();if(!root.has("version")||root.get("version").getAsInt()!=1)throw new IOException("Unsupported binding format");
            var array=root.getAsJsonArray("bindings");if(array==null||array.size()>LIMIT)throw new IOException("Invalid binding count");
            var result=new ArrayList<ActionBind>();var ids=new HashSet<UUID>();
            for(var item:array){var data=item.getAsJsonObject();var bind=new ActionBind(UUID.fromString(data.get("id").getAsString()),data.get("name").getAsString(),data.get("key").getAsString(),ActionBind.Kind.valueOf(data.get("kind").getAsString()),data.get("value").getAsString(),data.get("enabled").getAsBoolean());if(!ids.add(bind.id()))throw new IOException("Duplicate binding");result.add(bind);}
            return List.copyOf(result);
        });if(loaded!=null)entries=loaded;
    }
    public void put(ActionBind binding) throws IOException {
        var next=new ArrayList<>(entries);int index=-1;for(int i=0;i<next.size();i++)if(next.get(i).id().equals(binding.id()))index=i;
        if(index<0){if(next.size()>=LIMIT)throw new IllegalStateException("Binding limit reached");next.add(binding);}else next.set(index,binding);write(next);
    }
    public void remove(UUID id) throws IOException {var next=new ArrayList<>(entries);next.removeIf(bind->bind.id().equals(id));write(next);}
    private void write(List<ActionBind> next) throws IOException {
        var root=new JsonObject();root.addProperty("version",1);var array=new JsonArray();
        for(var bind:next){var item=new JsonObject();item.addProperty("id",bind.id().toString());item.addProperty("name",bind.name());item.addProperty("key",bind.key());item.addProperty("kind",bind.kind().name());item.addProperty("value",bind.value());item.addProperty("enabled",bind.enabled());array.add(item);}
        root.add("bindings",array);file.write(new Gson().toJson(root));entries=List.copyOf(next);
    }
}
