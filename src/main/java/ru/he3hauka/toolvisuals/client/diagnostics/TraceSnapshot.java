package ru.he3hauka.toolvisuals.client.diagnostics;

import java.util.*;
import ru.he3hauka.toolvisuals.client.config.ClientConfig;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

record TraceSnapshot(long revision,Map<String,String> fields) {
    static TraceSnapshot capture(ClientConfig config){
        var fields=new TreeMap<String,String>();
        fields.put("persistence",config.persistenceStatus().name());
        for(var module:VisualModule.values())fields.put("module."+module.id,Boolean.toString(config.active(module)));
        for(var setting:VisualSetting.values())fields.put("setting."+setting.id,Double.toString(config.value(setting)));
        for(var element:ru.he3hauka.toolvisuals.client.hud.layout.HudElement.values())fields.put("hud_scale."+element.id,Double.toString(config.hudScale(element)));
        for(var option:RenderOption.values())fields.put("filter."+option.id,Boolean.toString(config.selected(option)));
        return new TraceSnapshot(config.revision(),Collections.unmodifiableMap(fields));
    }

    List<String> header(){return fields.entrySet().stream().map(entry->entry.getKey()+"="+entry.getValue()).toList();}
    void changes(TraceSnapshot before,TraceSession session){
        for(var entry:fields.entrySet()){
            var old=before.fields.get(entry.getKey());
            if(!Objects.equals(old,entry.getValue()))session.event("CONFIG","changed",entry.getKey()+" "+old+" -> "+entry.getValue());
        }
    }
}
