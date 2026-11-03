package ru.he3hauka.toolvisuals.client.config;

import java.util.LinkedHashSet;

import ru.he3hauka.toolvisuals.client.chat.layout.ChatSize;
import ru.he3hauka.toolvisuals.client.config.color.ColorSettings;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

final class ConfigMigrations {
    static final java.util.List<VisualModule> DEFAULTS=java.util.List.of(VisualModule.OPTIMIZATION,VisualModule.AUTO_SPRINT,VisualModule.FAST_SHADERS,VisualModule.CLIENT_BADGE,VisualModule.CAPES,VisualModule.COSMETICS,VisualModule.EXTRA_ANIMATIONS,VisualModule.DEATH_ASCENSION,VisualModule.CRYSTAL_OPTIMIZER,VisualModule.APPLE_SKIN,VisualModule.BETTER_HUD,VisualModule.ZOOM,VisualModule.FULL_BRIGHT,VisualModule.BLOCK_HIGHLIGHT,VisualModule.INFO_HUD,VisualModule.DROP_PROTECT,VisualModule.SHULKER_TOOLTIP,VisualModule.GPS);
    static final int VERSION=DEFAULTS.size();
    static final int CUSTOM_HAND_VERSION=1;
    private ConfigMigrations(){}
    static java.util.EnumSet<VisualModule> defaultModules() {
        var modules=java.util.EnumSet.copyOf(DEFAULTS);
        modules.removeAll(java.util.List.of(VisualModule.DROP_PROTECT,VisualModule.SHULKER_TOOLTIP,VisualModule.BETTER_HUD,VisualModule.BLOCK_HIGHLIGHT,VisualModule.APPLE_SKIN));
        return modules;
    }
    static void apply(ConfigSnapshot saved){
        if(saved.customHandVersion<CUSTOM_HAND_VERSION) {
            if(saved.values!=null) {
                saved.values=new java.util.LinkedHashMap<>(saved.values);
                for(var property:java.util.List.of(ru.he3hauka.toolvisuals.client.hand.HandProperty.X,
                        ru.he3hauka.toolvisuals.client.hand.HandProperty.Y,ru.he3hauka.toolvisuals.client.hand.HandProperty.Z,
                        ru.he3hauka.toolvisuals.client.hand.HandProperty.SCALE)) {
                    Double value=saved.values.get(property.right.id);
                    if(value!=null)saved.values.putIfAbsent(property.left.id,value);
                }
                Double swing=saved.values.remove(VisualSetting.NO_SWING.id);
                if(swing!=null&&swing>.5) {
                    saved.values.putIfAbsent(VisualSetting.HAND_NO_SWING.id,1d);
                    saved.values.putIfAbsent(VisualSetting.LEFT_HAND_NO_SWING.id,1d);
                }
            }
            saved.customHandVersion=CUSTOM_HAND_VERSION;
        }
        if(saved.values!=null){
            saved.values=new java.util.LinkedHashMap<>(saved.values);
            Double width=saved.values.remove("better_hud_chat_width"),height=saved.values.remove("better_hud_chat_height");
            saved.values.remove("better_hud_chat_resize");
            if(saved.chatSize==null)saved.chatSize=new ChatSize(width==null?0:width,height==null?0:height);
        }
        var modules=new LinkedHashSet<String>();if(saved.modules!=null)modules.addAll(saved.modules);
        var enabledDefaults=defaultModules();
        for(int index=0;index<DEFAULTS.size();index++)if(saved.moduleDefaultsVersion<index+1&&enabledDefaults.contains(DEFAULTS.get(index)))modules.add(DEFAULTS.get(index).id);
        saved.modules=modules;
        if(saved.moduleDefaultsVersion<3&&saved.values!=null){
            saved.values.remove(VisualSetting.CHUNK_ANIMATION_UPDATES.id);
            if(Double.valueOf(3).equals(saved.values.get(VisualSetting.TARGET_HUD_HOLD.id)))saved.values.remove(VisualSetting.TARGET_HUD_HOLD.id);
        }
        if(saved.moduleDefaultsVersion<13&&saved.values!=null){
            for(var setting:java.util.List.of(VisualSetting.HEALTH_INDICATOR_HIGH_COLOR,VisualSetting.HEALTH_INDICATOR_MEDIUM_COLOR,VisualSetting.HEALTH_INDICATOR_LOW_COLOR)){
                Double value=saved.values.get(setting.id);
                if(value!=null&&value>=0&&value<=5)saved.values.put(setting.id,(double)ColorSettings.PRESETS[value.intValue()]);
            }
        }
        saved.moduleDefaultsVersion=VERSION;
    }
}
