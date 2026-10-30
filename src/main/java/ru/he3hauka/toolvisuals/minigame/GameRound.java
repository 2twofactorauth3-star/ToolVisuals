package ru.he3hauka.toolvisuals.minigame;

import java.util.Arrays;

import ru.he3hauka.toolvisuals.minigame.board.GridBoard;
import ru.he3hauka.toolvisuals.minigame.sling.SlingGame;

public final class GameRound {
    public final GameKind kind;
    public final long seed;
    public final int revision, turn, winner;
    public final GridBoard grid;
    public final SlingGame.Level first, second;
    public final SlingGame.Shot shot;
    private final int[] history;

    public GameRound(GameKind kind, long seed) { this(kind, seed, new int[0]); }

    public GameRound(GameKind kind, long seed, int[] history) {
        if (history.length % 2 != 0 || history.length / 2 > kind.maximumMoves) throw new IllegalArgumentException("Invalid game history");
        this.kind = kind; this.seed = seed; this.history = history.clone(); this.revision = history.length / 2;
        var board = kind == GameKind.ANGRY_BIRDS ? null : new GridBoard(kind);
        SlingGame.Level a = kind == GameKind.ANGRY_BIRDS ? SlingGame.level(seed) : null, b = a;
        SlingGame.Shot latest = null;
        for (int i = 0; i < history.length; i += 2) {
            if (board != null) {
                if (history[i + 1] != 0) throw new IllegalArgumentException("Unexpected grid parameter");
                board = board.play(history[i]);
            } else {
                boolean one = i % 4 == 0;
                latest = SlingGame.shoot(one ? a : b, history[i], history[i + 1]);
                if (one) a = latest.after(); else b = latest.after();
            }
        }
        this.grid = board; this.first = a; this.second = b; this.shot = latest; this.turn = revision % 2 + 1;
        this.winner = board != null ? board.winner : revision < kind.maximumMoves ? 0 : a.score() == b.score() ? 3 : a.score() > b.score() ? 1 : 2;
    }

    public int[] history() { return history.clone(); }

    public GameRound play(int first, int second) {
        if (winner != 0) throw new IllegalArgumentException("Game is already finished");
        int[] next = Arrays.copyOf(history, history.length + 2); next[history.length] = first; next[history.length + 1] = second;
        return new GameRound(kind, seed, next);
    }

    public int[] computerMove() {
        if (winner != 0) return new int[]{-1, 0};
        return grid != null ? new int[]{grid.bestMove(), 0} : SlingGame.aim(turn == 1 ? first : second, seed + revision * 31L);
    }
}
