package ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public final class ProfileEvidence {
    public enum Kind { ALLOCATION,CPU,BLOCK,GC_PAUSE }
    public record Sample(long from,long to,Kind kind,long thread,String subject,long weight,List<String> stack) {
        public Sample { stack=List.copyOf(stack); }
    }
    public record Hotspot(String subject,long weight,int samples,List<String> stack) {}
    private static final int LIMIT=4096;
    private final ArrayDeque<Sample> samples=new ArrayDeque<>();
    private long discarded;
    public synchronized void add(Sample sample) {
        if(samples.size()==LIMIT){samples.removeFirst();discarded++;}
        samples.addLast(sample);
    }
    public synchronized List<Hotspot> top(Kind kind,long from,long to,long owner,int limit) {
        var groups=new HashMap<String,Hotspot>();
        for(var sample:samples) {
            if(sample.kind!=kind||sample.to<from||sample.from>to||owner>0&&sample.thread!=owner)continue;
            String key=sample.subject+'\0'+String.join("\n",sample.stack);
            var previous=groups.get(key);
            groups.put(key,new Hotspot(sample.subject,(previous==null?0:previous.weight)+sample.weight,(previous==null?0:previous.samples)+1,sample.stack));
        }
        return groups.values().stream().sorted(Comparator.comparingLong(Hotspot::weight).reversed()).limit(limit).toList();
    }
    public synchronized long paused(long from,long to) {
        var ranges=new ArrayList<Sample>();
        for(var sample:samples)if(sample.kind==Kind.GC_PAUSE&&sample.to>from&&sample.from<to)ranges.add(sample);
        ranges.sort(Comparator.comparingLong(Sample::from));
        long end=from,total=0;
        for(var sample:ranges){long begin=Math.max(Math.max(from,sample.from),end),finish=Math.min(to,sample.to);if(finish>begin){total+=finish-begin;end=finish;}}
        return total;
    }
    public synchronized long discarded() { return discarded; }
    public synchronized void clear() { samples.clear();discarded=0; }
}
