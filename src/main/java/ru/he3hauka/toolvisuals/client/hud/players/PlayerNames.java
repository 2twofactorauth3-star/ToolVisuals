package ru.he3hauka.toolvisuals.client.hud.players;

public final class PlayerNames {
    private PlayerNames(){}
    public static String shorten(String name,int limit){
        int count=Math.clamp(limit,14,18);
        return name.codePointCount(0,name.length())<=count?name:name.substring(0,name.offsetByCodePoints(0,count))+"\u2026";
    }
}
