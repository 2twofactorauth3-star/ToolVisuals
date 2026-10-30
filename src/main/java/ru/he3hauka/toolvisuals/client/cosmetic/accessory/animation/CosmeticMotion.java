package ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation;

import java.util.*;

import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;

import static ru.he3hauka.toolvisuals.client.cosmetic.accessory.animation.MotionSample.*;

public final class CosmeticMotion {
    public static final int LIMIT=256;
    private static final class Track {
        final double[] previous=new double[CHANNELS],current=new double[CHANNELS];
        long tick;double phase,previousPhase,stride,previousStride,landingVelocity,lastVertical;boolean grounded;
    }
    private final Map<UUID,Track> tracks=new LinkedHashMap<>();
    public MotionSample sample(UUID actor,UUID owner,long tick,float partial,AccessoryLook look,MotionInput input,double strength,boolean headSway){
        var track=tracks.get(actor);
        if(track==null||tick<track.tick||tick-track.tick>10||input.teleport&&track.tick!=tick){
            track=new Track();track.tick=tick-1;track.grounded=input.grounded;track.lastVertical=input.vertical;
            track.phase=track.previousPhase=(Math.floorMod(tick,24000)/20d*frequency(look)*look.speed()+(actor.getLeastSignificantBits()&1023)/1024d)%1;
            tracks.put(actor,track);
        }
        if(track.tick!=tick){
            double[] goal=goals(look,input,strength,headSway);
            int elapsed=(int)Math.clamp(tick-track.tick,1,10);
            for(int step=0;step<elapsed;step++){
                System.arraycopy(track.current,0,track.previous,0,CHANNELS);track.previousPhase=track.phase;track.previousStride=track.stride;
                for(int channel=0;channel<CHANNELS;channel++)if(channel!=LANDING){
                    double response=channel==TURN||channel==HEAD_TURN?15:channel==RISE||channel==FALL?12:9;
                    track.current[channel]+=(goal[channel]-track.current[channel])*(1-Math.exp(-response*.05));
                }
                if(step==elapsed-1&&input.grounded&&!track.grounded&&track.lastVertical<-.07&&!input.teleport)track.landingVelocity+=Math.min(1,-track.lastVertical/.7)*9;
                track.landingVelocity+=(-90*track.current[LANDING]-13*track.landingVelocity)*.05;
                track.current[LANDING]=Math.clamp(track.current[LANDING]+track.landingVelocity*.05,-.7,.7);
                double activity=track.current[WEIGHT];
                double drive=1+activity*(track.current[MOVE]*.7+track.current[RUN]*.25+track.current[FALL]*.4+track.current[RISE]*.25);
                drive*=1-activity*.50*track.current[GLIDE];drive*=1-activity*.35*track.current[SWIM];
                track.phase+=frequency(look)*look.speed()*Math.clamp(drive,.15,3)*.05;
                track.stride+=input.teleport?0:input.speed*2.8/Math.TAU;
                if(track.phase>=1024){track.phase-=1024;track.previousPhase-=1024;}
                if(track.stride>=1024){track.stride-=1024;track.previousStride-=1024;}
            }
            track.tick=tick;track.grounded=input.grounded;track.lastVertical=input.vertical;
        }
        if(tracks.size()>LIMIT){var iterator=tracks.keySet().iterator();while(iterator.hasNext())if(!iterator.next().equals(owner)){iterator.remove();break;}}
        double t=MotionInput.bounded(partial,0,1);
        return new MotionSample(true,mix(track.previousPhase,track.phase,t),mix(track.previousStride,track.stride,t),track.previous,track.current,t);
    }
    private static double[] goals(AccessoryLook look,MotionInput input,double strength,boolean headSway){
        var values=new double[CHANNELS];
        if(!input.teleport){
            values[MOVE]=Math.clamp(input.speed/.24,0,1);values[RUN]=input.sprinting?values[MOVE]:0;
            values[RISE]=input.grounded||input.gliding||input.swimming?0:Math.clamp(input.vertical/.42,0,1);
            values[FALL]=input.grounded||input.gliding||input.swimming?0:Math.clamp(-input.vertical/.75,0,1);
            values[GLIDE]=input.gliding?1:0;values[SWIM]=input.swimming?1:0;values[CROUCH]=input.crouching?1:0;values[TURN]=input.turn;values[HEAD_TURN]=input.headTurn;
        }
        values[WEIGHT]=look.animated()?MotionInput.bounded(strength,0,1.5):0;values[HEAD_WEIGHT]=headSway?1:0;return values;
    }
    public static double frequency(AccessoryLook look){return BackMotion.of(look.back()).frequency;}
    private static double mix(double a,double b,double t){return a+(b-a)*t;}
    public void forget(UUID actor){tracks.remove(actor);}
    public void expire(long tick){tracks.values().removeIf(track->tick<track.tick||tick-track.tick>200);}
    public void clear(){tracks.clear();}
    public int size(){return tracks.size();}
}
