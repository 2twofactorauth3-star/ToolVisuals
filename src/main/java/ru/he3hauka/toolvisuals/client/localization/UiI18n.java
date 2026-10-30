package ru.he3hauka.toolvisuals.client.localization;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class UiI18n {
    private static final Map<UiLanguage,Map<String,String>> DICTIONARIES = load();
    private static volatile UiLanguage language = UiLanguage.ENGLISH;
    private UiI18n() {}
    public static void use(UiLanguage selected) { language = selected; }
    public static MutableComponent tr(String key,Object... args) {
        var selected=language;
        for(var argument:args)if(argument instanceof Component)
            return LocalizedComponents.format(selected.locale,text(selected,key),args);
        return Component.literal(text(selected,key,args));
    }
    public static String text(UiLanguage selected,String key,Object... args) {
        String template = DICTIONARIES.get(selected).getOrDefault(key,DICTIONARIES.get(UiLanguage.ENGLISH).getOrDefault(key,key));
        if (args.length == 0) return template;
        Object[] values = new Object[args.length];
        for (int i=0;i<args.length;i++) values[i] = args[i] instanceof Component component ? component.getString() : args[i];
        return String.format(selected.locale,template,values);
    }
    private static Map<UiLanguage,Map<String,String>> load() {
        var result = new EnumMap<UiLanguage,Map<String,String>>(UiLanguage.class);
        for (var language : UiLanguage.values()) {
            String resource = "/assets/toolvisuals/lang/" + language.id + ".json";
            try (var stream = UiI18n.class.getResourceAsStream(resource)) {
                if (stream == null) throw new IllegalStateException("Missing UI translations: " + resource);
                var values = new HashMap<String,String>();
                JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject()
                        .entrySet().forEach(entry -> values.put(entry.getKey(),entry.getValue().getAsString()));
                result.put(language,Map.copyOf(values));
            } catch (java.io.IOException error) { throw new IllegalStateException("Cannot load " + resource,error); }
        }
        return Map.copyOf(result);
    }
}
