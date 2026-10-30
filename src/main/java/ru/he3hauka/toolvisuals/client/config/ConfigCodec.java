package ru.he3hauka.toolvisuals.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class ConfigCodec {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private ConfigCodec(){}
    static ConfigSnapshot decode(JsonElement document){
        var saved=JSON.fromJson(document,ConfigSnapshot.class);
        if(saved==null)throw new IllegalArgumentException("Empty configuration");
        ConfigMigrations.apply(saved);return saved;
    }
    static JsonObject object(ConfigSnapshot snapshot){return JSON.toJsonTree(snapshot).getAsJsonObject();}
    static String encode(Object snapshot){return JSON.toJson(snapshot);}
}
