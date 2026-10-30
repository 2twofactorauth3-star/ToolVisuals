package ru.he3hauka.toolvisuals.animation;

public enum PairEmote {
    HUG(0,Emote.HUG,.64f), KISS(1,Emote.BLOW_KISS,.60f), HANDSHAKE(2,Emote.HANDSHAKE,.90f), HIGH_FIVE(3,Emote.CHEER,1f), DANCE(4,Emote.SIDE_STEP,1.05f), HEART(5,Emote.HAND_HEART,.82f);

    private static final PairEmote[] ALL = values();
    private static final java.util.Map<Integer,PairEmote> BY_ID=ru.he3hauka.toolvisuals.animation.network.WireCatalog.index(ALL,value->value.id);
    public final int id;
    public final String key = name().toLowerCase(java.util.Locale.ROOT);
    public final Emote animation;
    public final float spacing;
    PairEmote(int id,Emote animation,float spacing){this.id=id;this.animation=animation;this.spacing=spacing;}
    public static PairEmote byId(int id){return BY_ID.get(id);}
}
