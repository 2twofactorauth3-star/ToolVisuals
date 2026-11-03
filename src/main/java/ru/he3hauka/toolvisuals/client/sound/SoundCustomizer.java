package ru.he3hauka.toolvisuals.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.SOUND_CUSTOMIZER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class SoundCustomizer {
    public enum Channel { HIT, MODULE, NOTICE }
    private static long lastHit,lastNotice;
    private static final HitSoundFilter HIT_FILTER=new HitSoundFilter();
    private static Object connection;
    private SoundCustomizer() {}
    public static void play(Channel channel,boolean preview,float pitchFactor) {
        var config=ToolVisualsClient.CONFIG;
        if(!preview&&!config.active(SOUND_CUSTOMIZER))return;
        var flag=switch(channel){case HIT->SOUND_HITS;case MODULE->SOUND_MODULES;case NOTICE->SOUND_NOTICES;};
        if(!preview&&!config.flag(flag))return;
        var kind=switch(channel){case HIT->SOUND_HIT_KIND;case MODULE->SOUND_MODULE_KIND;case NOTICE->SOUND_NOTICE_KIND;};
        var volume=switch(channel){case HIT->SOUND_HIT_VOLUME;case MODULE->SOUND_MODULE_VOLUME;case NOTICE->SOUND_NOTICE_VOLUME;};
        int selected=(int)config.value(kind);
        SoundEvent sound=selected==SoundPreset.CUSTOM?ru.he3hauka.toolvisuals.client.sound.library.CustomSounds.event(channel):SoundPreset.event(selected);
        if(sound==null)sound=SoundPreset.event(0);
        if(config.number(volume)>0)Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(sound,Math.clamp(config.number(SOUND_PITCH)*pitchFactor,.5f,2f),config.number(volume)));
    }
    public static void module(boolean enabled) { play(Channel.MODULE,false,enabled?1.1f:.9f); }
    public static void notice() {
        long now=System.nanoTime();if(now-lastNotice<120_000_000L)return;lastNotice=now;play(Channel.NOTICE,false,1);
    }
    public static void hit() {
        long now=System.nanoTime();if(now-lastHit<60_000_000L)return;lastHit=now;play(Channel.HIT,false,1);
    }
    private static boolean replacesHits() {
        return ToolVisualsClient.CONFIG.active(SOUND_CUSTOMIZER)&&ToolVisualsClient.CONFIG.flag(SOUND_HITS);
    }
    public static void tick(Minecraft client) {
        var current=client.getConnection();
        if(current!=connection||client.level==null||!replacesHits())HIT_FILTER.clear();
        connection=current;
    }
    public static void attacking(Player source,Entity target) {
        var client=Minecraft.getInstance();
        if(replacesHits()&&source==client.player&&target instanceof LivingEntity&&target!=source)
            HIT_FILTER.attack(source.position(),System.nanoTime());
    }
    public static boolean suppressVanilla(SoundInstance sound) {
        var client=Minecraft.getInstance();
        if(!replacesHits()||client.player==null||client.level==null)return false;
        return HIT_FILTER.suppress(sound.getIdentifier(),sound.isRelative(),new Vec3(sound.getX(),sound.getY(),sound.getZ()),
                client.player.position(),System.nanoTime());
    }
    public static void damage(ClientboundDamageEventPacket packet) {
        var client=Minecraft.getInstance();if(client.level==null||client.player==null||!replacesHits())return;
        var victim=client.level.getEntity(packet.entityId());
        boolean own=victim instanceof LivingEntity&&victim!=client.player&&packet.getSource(client.level).getEntity()==client.player;
        if(victim!=null)HIT_FILTER.damage(victim.getId(),victim.position(),own,System.nanoTime());
        if(own)hit();
    }
}
