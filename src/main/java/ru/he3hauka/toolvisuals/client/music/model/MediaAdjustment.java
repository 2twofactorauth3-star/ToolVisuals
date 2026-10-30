package ru.he3hauka.toolvisuals.client.music.model;

public record MediaAdjustment(MediaSnapshot snapshot,MediaSession session,MediaAction action,double fraction) {
    public MediaAdjustment {
        if (action!=MediaAction.SEEK&&action!=MediaAction.VOLUME || !action.available(session) || !Double.isFinite(fraction))
            throw new IllegalArgumentException("Invalid media slider adjustment");
        fraction=Math.clamp(fraction,0,1);
    }
    public MediaAdjustment move(double fraction) { return new MediaAdjustment(snapshot,session,action,fraction); }
    public boolean matches(MediaSnapshot current) {
        var selected=current.selected();
        if (selected==null||current.generation()!=snapshot.generation()||!selected.id().equals(session.id())||!action.available(selected))return false;
        return action==MediaAction.SEEK?selected.track().equals(session.track()):selected.volume().target().equals(session.volume().target());
    }
}
