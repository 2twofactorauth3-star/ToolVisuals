package ru.he3hauka.toolvisuals.client.hud.layout;

import java.util.*;
import java.util.function.Predicate;

import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.hud.info.InfoHudPart;

public final class HudDragging {
    private static final HudElement[] PICK_ORDER={HudElement.ARMOR,HudElement.ARMOR_2,HudElement.ARMOR_3,HudElement.ARMOR_4,HudElement.MODULES,
            HudElement.INFO_BRAND,HudElement.INFO_PING,HudElement.INFO_FPS,HudElement.INFO_BPS,HudElement.INFO_CLOCK,HudElement.INFO_COORDINATES,
            HudElement.MUSIC,HudElement.INFO,HudElement.INVENTORY,HudElement.POTIONS,HudElement.COOLDOWNS,HudElement.FRIENDS,HudElement.CLIENTS,HudElement.SESSION,HudElement.KEYS,HudElement.HIT_HEALTH,HudElement.TARGET,HudElement.BOSSES,HudElement.PRESENTS};
    private final Map<HudElement,HudPosition> pending=new EnumMap<>(HudElement.class);
    private HudElement selected;
    private HudPlacement.Bounds box;
    private float grabX,grabY;
    public static HudElement pick(Map<HudElement,HudPlacement.Bounds> bounds,Predicate<HudElement> eligible,double x,double y){
        for(var element:PICK_ORDER){var box=bounds.get(element);if(box!=null&&eligible.test(element)&&box.contains(x,y))return element;}return null;
    }
    public static boolean enabled(HudElement element,ClientConfig config){
        if(!config.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.HUD_EDITOR))return false;
        if(!config.active(element.module))return false;
        var part=InfoHudPart.forElement(element);
        if(part!=null)return part.enabled(config)&&config.flag(part.separate);
        if(element==HudElement.INFO)return InfoHudPart.hasJoined(config);
        return element!=HudElement.MODULES||config.flag(VisualSetting.HUD_MODULES);
    }
    public boolean begin(Map<HudElement,HudPlacement.Bounds> bounds,Predicate<HudElement> eligible,double x,double y){
        selected=pick(bounds,eligible,x,y);if(selected==null)return false;box=bounds.get(selected);grabX=(float)x-box.x;grabY=(float)y-box.y;return true;
    }
    public boolean move(double x,double y,int width,int height,Predicate<HudElement> eligible){
        if(selected==null)return false;if(!eligible.test(selected)){pending.remove(selected);selected=null;return false;}
        pending.put(selected,HudPosition.fromPixels((float)x-grabX,(float)y-grabY,width,height,box.width,box.height));return true;
    }
    public boolean dragging(){return selected!=null;}
    public HudElement selected(){return selected;}
    public void resized(HudElement element,HudPlacement.Bounds bounds,double x,double y){
        if(selected!=element)return;
        box=bounds;grabX=(float)x-box.x;grabY=(float)y-box.y;pending.remove(element);
    }
    public HudPosition position(HudElement element,HudPosition saved){return pending.getOrDefault(element,saved);}
    public Map<HudElement,HudPosition> finish(){var changes=Map.copyOf(pending);pending.clear();selected=null;box=null;return changes;}
}
