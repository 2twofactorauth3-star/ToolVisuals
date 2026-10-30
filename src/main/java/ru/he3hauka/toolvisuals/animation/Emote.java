package ru.he3hauka.toolvisuals.animation;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static ru.he3hauka.toolvisuals.animation.EmoteGroup.*;

public enum Emote {
    BACKFLIP(0,"backflip",40,TRICKS), FRONTFLIP(1,"frontflip",40,TRICKS), WAVE(2,"wave",64,SOCIAL), CLAP(3,"clap",64,SOCIAL),
    DAB(4,"dab",48,TRICKS), BOW(5,"bow",48,SOCIAL), SALUTE(6,"salute",48,SOCIAL), DANCE(7,"dance",120,JOY),
    SPIN(8,"spin",56,TRICKS), CELEBRATE(9,"celebrate",64,JOY), FACEPALM(10,"facepalm",56,SOCIAL), SIT(11,"sit",120,MOOD),
    HANDSHAKE(12,"handshake",72,SOCIAL), POINT(13,"point",60,SOCIAL), THINK(14,"think",80,SOCIAL), SHRUG(15,"shrug",64,SOCIAL),
    YES(16,"yes",56,SOCIAL), NO(17,"no",56,SOCIAL), SHUSH(18,"shush",64,SOCIAL),
    CHEER(19,"cheer",80,JOY), DISCO(20,"disco",120,JOY), SHUFFLE(21,"shuffle",120,JOY), GROOVE(22,"groove",120,JOY),
    CLAP_DANCE(23,"clap_dance",120,JOY), SIDE_STEP(24,"side_step",120,JOY), WAVE_DANCE(25,"wave_dance",120,JOY),
    BOUNCE(26,"bounce",96,JOY), VICTORY(27,"victory",80,JOY), HAPPY_SPIN(28,"happy_spin",80,JOY),
    HEART(29,"heart",96,AFFECTION), BLOW_KISS(30,"blow_kiss",80,AFFECTION), HUG(31,"hug",96,AFFECTION), SHY(32,"shy",80,AFFECTION),
    THANK_YOU(33,"thank_you",80,AFFECTION), FLATTERED(34,"flattered",80,AFFECTION), DEVOTION(35,"devotion",80,AFFECTION),
    HAND_HEART(36,"hand_heart",96,AFFECTION), HEART_PULSE(37,"heart_pulse",120,AFFECTION), WELCOME(38,"welcome",64,AFFECTION),
    WARM_WAVE(39,"warm_wave",80,AFFECTION), BASHFUL_BOW(40,"bashful_bow",80,AFFECTION),
    RELAX(41,"relax",120,MOOD), CONFUSED(42,"confused",80,MOOD), SURPRISED(43,"surprised",64,MOOD), ANGRY(44,"angry",96,MOOD),
    SAD(45,"sad",120,MOOD), SCARED(46,"scared",80,MOOD), SLEEPY(47,"sleepy",120,MOOD), IMPATIENT(48,"impatient",80,MOOD),
    PROUD(49,"proud",96,MOOD), DISAPPOINTED(50,"disappointed",80,MOOD), BORED(51,"bored",120,MOOD),
    CARTWHEEL(52,"cartwheel",56,TRICKS), HANDSTAND(53,"handstand",100,TRICKS), SIDEFLIP(54,"sideflip",48,TRICKS),
    HEEL_CLICK(55,"heel_click",56,TRICKS), WINDMILL(56,"windmill",96,TRICKS), ROBOT(57,"robot",120,TRICKS),
    LEAN(58,"lean",80,TRICKS), STAR_JUMP(59,"star_jump",56,TRICKS);

    public static final int LEGACY_COUNT = 12;
    private static final Emote[] ALL = values();
    private static final Map<Integer,Emote> BY_ID=ru.he3hauka.toolvisuals.animation.network.WireCatalog.index(ALL,value->value.id);
    private static final Map<EmoteGroup,List<Emote>> GROUPS = groups();
    public final int id,duration;
    public final String key;
    public final EmoteGroup group;
    Emote(int id,String key,int duration,EmoteGroup group){this.id=id;this.key=key;this.duration=duration;this.group=group;}
    public static Emote byId(int id){return BY_ID.get(id);}
    public static List<Emote> in(EmoteGroup group){return GROUPS.get(group);}
    private static Map<EmoteGroup,List<Emote>> groups(){var groups=new EnumMap<EmoteGroup,List<Emote>>(EmoteGroup.class);for(var group:EmoteGroup.values())groups.put(group,Arrays.stream(ALL).filter(emote->emote.group==group).toList());return Map.copyOf(groups);}
}
