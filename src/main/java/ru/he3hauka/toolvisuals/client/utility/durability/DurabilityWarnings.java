package ru.he3hauka.toolvisuals.client.utility.durability;

import java.util.*;

public final class DurabilityWarnings {
    private record Seen(String item,int damage,long warned){}
    private final Map<String,Seen> seen=new HashMap<>();
    public boolean shouldWarn(String slot,String item,int damage,int maximum,int threshold,long tick,long repeat) {
        if(maximum<=0||maximum-damage>maximum*threshold/100d){seen.remove(slot);return false;}
        var old=seen.get(slot);
        boolean warn=old==null||!old.item.equals(item)||damage>old.damage&&tick-old.warned>=repeat;
        seen.put(slot,new Seen(item,damage,warn?tick:old.warned));return warn;
    }
    public void clear(){seen.clear();}
}
