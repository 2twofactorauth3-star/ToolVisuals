package ru.he3hauka.toolvisuals.build.compatibility;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import ru.he3hauka.toolvisuals.client.compatibility.lunar.LunarChatCompatibility;

public final class ViaFabricPlusCompatibility {
    private ViaFabricPlusCompatibility() {}

    public static byte[] configure(byte[] bytes) throws IOException {
        var config=JsonParser.parseString(new String(bytes,StandardCharsets.UTF_8)).getAsJsonObject();
        if(!config.has("plugin"))throw new IOException("ViaFabricPlus mixin plugin is missing");
        String plugin=config.get("plugin").getAsString();
        if(LunarChatCompatibility.COMPATIBILITY_PLUGIN.equals(plugin))return bytes;
        if(!LunarChatCompatibility.ORIGINAL_PLUGIN.equals(plugin))throw new IOException("Unrecognized ViaFabricPlus mixin plugin: "+plugin);
        config.addProperty("plugin",LunarChatCompatibility.COMPATIBILITY_PLUGIN);
        return config.toString().getBytes(StandardCharsets.UTF_8);
    }
}
