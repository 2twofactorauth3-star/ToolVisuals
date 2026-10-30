package ru.he3hauka.toolvisuals.client.animation.death;

import java.util.*;

public final class AscensionTimeline<T> {
    public static final int LIMIT=24;
    public record Soul<T>(UUID actor,int entity,T appearance,double x,double y,double z,double began,double duration,double height){}
    public record Frame(double rise,float alpha,double progress){}
    private final Map<UUID,Soul<T>> souls=new LinkedHashMap<>();
    public boolean start(UUID actor,int entity,T appearance,double x,double y,double z,double now,double duration,double height){
        Objects.requireNonNull(actor);Objects.requireNonNull(appearance);
        if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||!Double.isFinite(now)||!Double.isFinite(duration)||!Double.isFinite(height)||duration<1.5||duration>6||height<1||height>10)throw new IllegalArgumentException("Invalid ascension");
        var old=souls.get(actor);if(old!=null&&now>=old.began&&now-old.began<.5)return false;
        souls.remove(actor);souls.put(actor,new Soul<>(actor,entity,appearance,x,y,z,now,duration,height));
        if(souls.size()>LIMIT)souls.remove(souls.keySet().iterator().next());return true;
    }
    public static Frame frame(Soul<?> soul,double now){
        double t=(now-soul.began)/soul.duration;if(!Double.isFinite(t)||t<0||t>=1)return null;
        double rise=ease(t)*soul.height;float alpha=(float)(.85*(1-ease((t-.35)/.65)));
        return new Frame(rise,alpha,t);
    }
    public static double ease(double value){double t=Math.clamp(value,0,1);return t*t*(3-2*t);}
    public List<Soul<T>> active(double now){souls.values().removeIf(soul->frame(soul,now)==null);return List.copyOf(souls.values());}
    public boolean containsEntity(int entity){return souls.values().stream().anyMatch(soul->soul.entity==entity);}
    public void clear(){souls.clear();}
    public int size(){return souls.size();}
}
