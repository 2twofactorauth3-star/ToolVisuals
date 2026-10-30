package ru.he3hauka.toolvisuals.client.hud.cooldown;

import java.util.*;

public final class CooldownTimeline {
    public static final class Entry {
        public final String group;
        public final int duration;
        public final float remaining;
        Entry(String group,int duration,float remaining){this.group=group;this.duration=duration;this.remaining=remaining;}
    }
    private record Timer(long end,int duration) {}
    private final Map<String,Timer> timers=new LinkedHashMap<>();
    private long tick;
    public void start(String group,int duration) {
        if(group==null||group.isBlank()||group.equals("air")||group.equals("minecraft:air")) { timers.remove(group);return; }
        if(duration<=0){timers.remove(group);return;}
        if(!timers.containsKey(group)&&timers.size()>=256)timers.remove(timers.keySet().iterator().next());
        timers.put(group,new Timer(tick+duration,duration));
    }
    public void tick(){tick++;timers.values().removeIf(t->t.end<=tick);}
    public void clear(){timers.clear();tick=0;}
    public List<Entry> entries(float partial) {
        float fraction=Math.clamp(partial,0,1);
        return timers.entrySet().stream().map(e->new Entry(e.getKey(),e.getValue().duration,Math.max(0,e.getValue().end-tick-fraction)))
                .filter(e->e.remaining>0).sorted(Comparator.comparingDouble(e->e.remaining)).toList();
    }
}
