package ru.he3hauka.toolvisuals.client.hud.modules;

public final class ModuleListLimit {
    public record Layout(int shown,int hidden,int rows){}
    private ModuleListLimit(){}
    public static Layout layout(int total,int limit,int capacity){
        total=Math.max(0,total);limit=Math.clamp(limit,5,10);capacity=Math.max(1,capacity);
        int shown=Math.min(total,Math.min(limit,capacity));if(total>shown&&shown==capacity)shown=Math.max(0,shown-1);
        int hidden=total-shown;return new Layout(shown,hidden,shown+(hidden>0?1:0));
    }
}
