package ru.he3hauka.toolvisuals.client.potion.tracker;

import java.util.*;

public final class BuffTimeline {
    public record Effect(String id,int amplifier,long remaining,long duration,boolean estimated){}
    public record Entry(UUID player,String name,List<Effect> effects){}
    private record Span(int amplifier,long until,long duration,boolean estimated){}
    private static final class Actor {
        String name;
        final Map<String,List<Span>> effects=new LinkedHashMap<>();
        Actor(String name){this.name=name;}
    }
    private final LinkedHashMap<UUID,Actor> actors=new LinkedHashMap<>();
    private UUID owner;
    public void reset(UUID owner){actors.clear();this.owner=owner;}
    public void remove(UUID player){actors.remove(player);}
    public void remove(UUID player,String effect){var actor=actors.get(player);if(actor!=null){actor.effects.remove(effect);if(actor.effects.isEmpty())actors.remove(player);}}
    public void add(UUID player,String name,String effect,int amplifier,int duration,boolean estimated,long now){
        if(amplifier<0||amplifier>255||duration< -1||duration==0)return;
        var actor=actors.get(player);
        if(actor==null){
            if(actors.size()>=128){UUID victim=null;for(UUID id:actors.keySet())if(!id.equals(owner)){victim=id;break;}if(victim==null)return;actors.remove(victim);}
            actor=new Actor(name);actors.put(player,actor);
        }
        actor.name=name;
        if(actor.effects.size()>=32&&!actor.effects.containsKey(effect))return;
        var spans=actor.effects.computeIfAbsent(effect,key->new ArrayList<>());long until=duration==-1?Long.MAX_VALUE:now+duration;
        for(int i=0;i<spans.size();i++){var old=spans.get(i);if(old.amplifier==amplifier){if(until>old.until||!estimated&&old.estimated)spans.set(i,new Span(amplifier,until,duration,estimated));return;}}
        spans.add(new Span(amplifier,until,duration,estimated));if(spans.size()>4){spans.sort(Comparator.comparingInt(Span::amplifier).reversed());spans.removeLast();}
    }
    public void confirmed(UUID player,String name,String effect,int amplifier,int duration,long now){remove(player,effect);add(player,name,effect,amplifier,duration,false,now);}
    public List<Entry> entries(long now){
        var result=new ArrayList<Entry>();var players=actors.entrySet().iterator();
        while(players.hasNext()){
            var entry=players.next();var effects=new ArrayList<Effect>();var types=entry.getValue().effects.entrySet().iterator();
            while(types.hasNext()){
                var type=types.next();type.getValue().removeIf(span->span.until<=now);if(type.getValue().isEmpty()){types.remove();continue;}
                var selected=type.getValue().stream().max(Comparator.comparingInt(Span::amplifier).thenComparingLong(Span::until)).orElseThrow();
                effects.add(new Effect(type.getKey(),selected.amplifier,selected.until==Long.MAX_VALUE?-1:selected.until-now,selected.duration,selected.estimated));
            }
            if(effects.isEmpty()){players.remove();continue;}result.add(new Entry(entry.getKey(),entry.getValue().name,List.copyOf(effects)));
        }
        return List.copyOf(result);
    }
}
