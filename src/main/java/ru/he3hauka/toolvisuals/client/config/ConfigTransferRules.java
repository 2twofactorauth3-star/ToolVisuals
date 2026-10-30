package ru.he3hauka.toolvisuals.client.config;

import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

final class ConfigTransferRules {
    static final java.util.Set<VisualModule> PERSONAL_MODULES=java.util.Set.of(VisualModule.AUCTION_HELPER,VisualModule.KEYBINDS,VisualModule.VOICE_CHAT,VisualModule.MINI_GAMES);
    private ConfigTransferRules(){}
    static String preset(ConfigSnapshot snapshot){
        var json=ConfigCodec.object(snapshot);
        for(var field:new String[]{"nickname","uiLanguage","menuBindingVersion","hudEditorToolbar","customTitleMenu","chatSize"})json.remove(field);
        var modules=json.getAsJsonArray("modules");
        for(int index=modules.size()-1;index>=0;index--){String id=modules.get(index).getAsString();if(PERSONAL_MODULES.stream().anyMatch(module->module.id.equals(id)))modules.remove(index);}
        var values=json.getAsJsonObject("values");for(var setting:VisualSetting.values())if(PERSONAL_MODULES.contains(setting.module))values.remove(setting.id);
        return ConfigCodec.encode(json);
    }
    static String history(ConfigSnapshot snapshot){
        var json=ConfigCodec.object(snapshot);json.remove("nickname");json.remove("menuBindingVersion");return ConfigCodec.encode(json);
    }
}
