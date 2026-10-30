package com.viaversion.viafabricplus.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public abstract class AbstractSave {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;

    public AbstractSave(final String name) {
        path = ViaFabricPlusImpl.INSTANCE.getPath().resolve(name + ".json");
    }

    public void init() {
        if (Files.exists(path)) {
            try (final BufferedReader reader = Files.newBufferedReader(path)) {
                final JsonObject object = GSON.fromJson(reader, JsonObject.class);
                if (object != null) {
                    read(object);
                } else {
                    ViaFabricPlusImpl.INSTANCE.getLogger().error("The file {} is empty!", path.getFileName());
                }
            } catch (Exception e) {
                ViaFabricPlusImpl.INSTANCE.getLogger().error("Failed to read file: {}!", path.getFileName(), e);
            }
        }
    }

    public void save() {
        try (final BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            final JsonObject object = new JsonObject();
            write(object);

            GSON.toJson(object, writer);
        } catch (Exception e) {
            ViaFabricPlusImpl.INSTANCE.getLogger().error("Failed to write file: {}!", path.getFileName(), e);
        }
    }

    public abstract void write(final JsonObject object);

    public abstract void read(final JsonObject object);

    public void postInit() {
    }

    public Path getPath() {
        return path;
    }

}
