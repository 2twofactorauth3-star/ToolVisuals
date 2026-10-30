package ru.he3hauka.toolvisuals.client.social.presence;

import java.util.*;

public final class PresenceRows {
    public record Entry(UUID id,String name){}
    private PresenceRows(){}
    public static List<Entry> select(Set<UUID> advertised,Map<UUID,String> listed,UUID self,boolean includeSelf){
        var entries=new ArrayList<Entry>();
        for(var id:advertised){
            var name=listed.get(id);
            if(name!=null&&(includeSelf||!id.equals(self)))entries.add(new Entry(id,name));
        }
        entries.sort(Comparator.comparing(Entry::name,String.CASE_INSENSITIVE_ORDER).thenComparing(Entry::id));
        return List.copyOf(entries);
    }
}
