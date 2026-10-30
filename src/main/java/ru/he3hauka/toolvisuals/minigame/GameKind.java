package ru.he3hauka.toolvisuals.minigame;

public enum GameKind {
    ANGRY_BIRDS(0, 6), TIC_TAC_TOE(1, 9), CONNECT_FOUR(2, 42);

    public final int id, maximumMoves;
    public final String key;

    GameKind(int id, int maximumMoves) {
        this.id = id; this.maximumMoves = maximumMoves; this.key = name().toLowerCase(java.util.Locale.ROOT);
    }

    public static GameKind byId(int id) {
        for (var kind : values()) if (kind.id == id) return kind;
        throw new IllegalArgumentException("Unknown minigame");
    }
}
