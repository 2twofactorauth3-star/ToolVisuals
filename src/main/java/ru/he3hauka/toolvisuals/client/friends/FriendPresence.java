package ru.he3hauka.toolvisuals.client.friends;

import java.util.*;

public final class FriendPresence {
    public static final class Entry {
        public final FriendStore.Friend friend;
        public final boolean online;
        public Entry(FriendStore.Friend friend,boolean online) { this.friend=friend;this.online=online; }
    }
    private FriendPresence() {}
    public static List<Entry> list(Collection<FriendStore.Friend> friends,Collection<String> players,boolean onlyOnline) {
        var online=new HashSet<String>();
        for(var name:players)online.add(name.toLowerCase(Locale.ROOT));
        var entries=new ArrayList<Entry>();
        for(var friend:friends) {
            boolean connected=online.contains(friend.name.toLowerCase(Locale.ROOT));
            if(connected||!onlyOnline)entries.add(new Entry(friend,connected));
        }
        entries.sort(Comparator.comparing((Entry entry)->!entry.online)
                .thenComparing(entry->entry.friend.name,String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(entries);
    }
}
