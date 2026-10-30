package ru.he3hauka.toolvisuals.client.voice;

public final class VoiceSession {
    public interface Engine {
        Object identity();
        boolean connected();
        boolean hasMicrophone();
        void startMicrophone();
        void stopMicrophone();
        void synchronize(VoicePolicy policy);
    }
    private Object identity;
    private VoicePolicy previous;
    private boolean connected;
    public void update(Engine engine,VoicePolicy policy){
        if(engine==null){identity=null;previous=null;connected=false;return;}
        boolean fresh=identity!=engine.identity(),online=engine.connected(),changed=fresh||!policy.equals(previous);
        if(changed)engine.synchronize(policy);
        if(!policy.enabled()){if(engine.hasMicrophone())engine.stopMicrophone();}
        else if(online&&(fresh||!connected||previous==null||!previous.enabled())&&!engine.hasMicrophone())engine.startMicrophone();
        identity=engine.identity();previous=policy;connected=online;
    }
}
