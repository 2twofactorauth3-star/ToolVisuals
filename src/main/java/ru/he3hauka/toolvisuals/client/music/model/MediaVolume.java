package ru.he3hauka.toolvisuals.client.music.model;

public record MediaVolume(double level,boolean muted,String target,String scope) {
    public static final MediaVolume UNAVAILABLE=new MediaVolume(-1,false,"","");
    public MediaVolume {
        if (!Double.isFinite(level) || level<0 || level>1 || target==null || !target.matches("[0-9a-f]{64}")
                || !"application".equals(scope) && !"browser".equals(scope)) {
            level=-1;muted=false;target="";scope="";
        }
    }
    public boolean available() { return !target.isEmpty(); }
}
