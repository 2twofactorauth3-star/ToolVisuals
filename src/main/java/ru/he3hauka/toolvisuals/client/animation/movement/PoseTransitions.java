package ru.he3hauka.toolvisuals.client.animation.movement;

import java.util.*;

import ru.he3hauka.toolvisuals.client.animation.pose.PlayerPose;

public final class PoseTransitions {
    private static final class Track {
        MovementPoses.Activity activity;boolean left;PlayerPose from,current;double changed,seen;
    }
    private final Map<UUID,Track> tracks=new LinkedHashMap<>();
    public PlayerPose frame(UUID actor,UUID owner,MovementPoses.Input input,double clock,double duration){
        if(!Double.isFinite(clock)||!Double.isFinite(duration)||duration<=0)return PlayerPose.EMPTY;
        if(input.activity()==MovementPoses.Activity.NONE&&!tracks.containsKey(actor))return PlayerPose.EMPTY;
        var track=tracks.computeIfAbsent(actor,id->new Track());
        if(track.activity!=input.activity()||track.left!=input.left()){
            track.from=track.current;track.changed=clock;track.activity=input.activity();track.left=input.left();
        }
        track.seen=clock;double t=Math.clamp((clock-track.changed)/duration,0,1);t=t*t*(3-2*t);
        var target=MovementPoses.sample(input);
        track.current=t>=1?target:PlayerPose.mix(track.from,target,t);
        if(t>=1&&input.activity()==MovementPoses.Activity.NONE){tracks.remove(actor);return PlayerPose.EMPTY;}
        if(tracks.size()>256){var iterator=tracks.keySet().iterator();while(iterator.hasNext())if(!iterator.next().equals(owner)){iterator.remove();break;}}
        return track.current;
    }
    public void remove(UUID actor){tracks.remove(actor);}
    public void expire(double clock){tracks.values().removeIf(track->clock<track.seen||clock-track.seen>200);}
    public void clear(){tracks.clear();}
    public int size(){return tracks.size();}
}
