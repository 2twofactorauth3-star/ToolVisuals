package ru.he3hauka.toolvisuals.client.sound;

import java.util.*;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class HitSoundFilter {
    private static final long ATTACK_WINDOW=1_500_000_000L,HURT_WINDOW=500_000_000L;
    private static final Set<String> ATTACKS=Set.of("entity.player.attack.strong","entity.player.attack.weak",
            "entity.player.attack.crit","entity.player.attack.knockback","entity.player.attack.sweep","entity.player.attack.nodamage");
    private static final class Hit {
        public final Vec3 target;
        public final long confirmed;
        private boolean muted;
        private Hit(Vec3 target,long confirmed) { this.target=target;this.confirmed=confirmed; }
    }
    private final Map<Integer,Hit> hits=new LinkedHashMap<>();
    private Vec3 origin;
    private long attacked;
    public void clear() { hits.clear();origin=null; }
    public void attack(Vec3 attacker,long now) { origin=attacker;attacked=now; }
    public void damage(int victim,Vec3 target,boolean own,long now) {
        hits.values().removeIf(hit->now-hit.confirmed>HURT_WINDOW);
        if(!own) { hits.remove(victim);return; }
        if(!hits.containsKey(victim)&&hits.size()>=16)hits.remove(hits.keySet().iterator().next());
        hits.put(victim,new Hit(target,now));
    }
    public boolean suppress(Identifier sound,boolean relative,Vec3 position,Vec3 player,long now) {
        if(relative||!sound.getNamespace().equals("minecraft"))return false;
        String path=sound.getPath();
        if(ATTACKS.contains(path))return origin!=null&&now-attacked>=0&&now-attacked<=ATTACK_WINDOW
                &&(position.distanceToSqr(origin)<=.36||position.distanceToSqr(player)<=.36);
        if(!path.startsWith("entity.")||!path.endsWith(".hurt"))return false;
        for(var hit:hits.values()) {
            double distance=position.distanceToSqr(hit.target);
            if(!hit.muted&&now-hit.confirmed>=0&&now-hit.confirmed<=HURT_WINDOW&&distance<=1
                    &&distance<position.distanceToSqr(player)) {
                hit.muted=true;return true;
            }
        }
        return false;
    }
}
