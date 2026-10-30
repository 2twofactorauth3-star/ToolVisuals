package ru.he3hauka.toolvisuals.client.keybinds;

import java.util.*;

import ru.he3hauka.toolvisuals.client.module.VisualModule;

public record ActionBind(UUID id,String name,String key,Kind kind,String value,boolean enabled) {
    public enum Kind { COMMAND, CHAT, MODULE }
    public ActionBind {
        Objects.requireNonNull(id);Objects.requireNonNull(kind);
        name=clean(name,48);key=clean(key,96);value=clean(value,256);
        if(name.isBlank()||!key.matches("key\\.(keyboard|mouse)\\.[a-z0-9._-]+"))throw new IllegalArgumentException("Invalid binding");
        if(kind==Kind.COMMAND){while(value.startsWith("/"))value=value.substring(1);}
        if(value.isBlank())throw new IllegalArgumentException("Empty action");
        if(kind==Kind.MODULE){String target=value;value=Arrays.stream(VisualModule.values()).filter(module->module.id.equalsIgnoreCase(target)||module.title.equalsIgnoreCase(target)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown module")).id;}
    }
    private static String clean(String text,int maximum){
        if(text==null||text.length()>maximum||text.codePoints().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Invalid binding text");return text.strip();
    }
    public ActionBind enabled(boolean selected){return new ActionBind(id,name,key,kind,value,selected);}
}
