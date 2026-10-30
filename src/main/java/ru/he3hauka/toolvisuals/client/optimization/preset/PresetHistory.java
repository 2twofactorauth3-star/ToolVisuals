package ru.he3hauka.toolvisuals.client.optimization.preset;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import ru.he3hauka.toolvisuals.client.storage.JsonFile;

public final class PresetHistory {
    private final JsonFile file;
    private final JsonObject versions;

    public PresetHistory(Path path) throws IOException {
        file=new JsonFile(path,16384);
        var saved=file.read(json->{
            if(!json.isJsonObject()||!json.getAsJsonObject().has("schema")||json.getAsJsonObject().get("schema").getAsInt()!=1
                    ||!json.getAsJsonObject().has("versions")||!json.getAsJsonObject().get("versions").isJsonObject())
                throw new IOException("Invalid performance preset history");
            var entries=json.getAsJsonObject().getAsJsonObject("versions");
            for(var entry:entries.entrySet()) if(!entry.getValue().isJsonPrimitive()||!entry.getValue().getAsJsonPrimitive().isBoolean())
                throw new IOException("Invalid performance preset entry");
            return entries.deepCopy();
        });
        versions=saved==null?new JsonObject():saved;
    }

    public boolean needsPreset(String version,boolean existingClient) {
        return !versions.has(version)&&(!existingClient||versions.size()>0);
    }

    public boolean recorded(String version) { return versions.has(version); }

    public void record(String version,boolean applied) throws IOException {
        var next=versions.deepCopy();next.addProperty(version,applied);
        var json=new JsonObject();json.addProperty("schema",1);json.add("versions",next);
        file.write(json.toString());versions.addProperty(version,applied);
    }
}
