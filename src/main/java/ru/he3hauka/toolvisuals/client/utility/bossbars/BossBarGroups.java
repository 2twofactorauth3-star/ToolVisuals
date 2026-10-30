package ru.he3hauka.toolvisuals.client.utility.bossbars;

import java.util.*;

public final class BossBarGroups {
    public record Entry(String title,int color,float progress,String overlay){}
    public record Row(String title,int color,float progress,int count){}
    private record Key(String title,int color,String overlay){}
    private BossBarGroups(){}
    public static List<Row> rows(List<Entry> entries,boolean group,int limit) {
        if(!group)return entries.stream().limit(Math.max(0,limit)).map(e->new Row(e.title,e.color,Math.clamp(e.progress,0,1),1)).toList();
        var groups=new LinkedHashMap<Key,List<Entry>>();
        entries.forEach(e->groups.computeIfAbsent(new Key(e.title,e.color,e.overlay),key->new ArrayList<>()).add(e));
        return groups.entrySet().stream().limit(Math.max(0,limit)).map(grouped->new Row(grouped.getKey().title,grouped.getKey().color,(float)grouped.getValue().stream().mapToDouble(e->Math.clamp(e.progress,0,1)).average().orElse(0),grouped.getValue().size())).toList();
    }
}
