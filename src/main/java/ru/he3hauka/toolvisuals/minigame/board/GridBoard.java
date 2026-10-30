package ru.he3hauka.toolvisuals.minigame.board;

import java.util.Arrays;

import ru.he3hauka.toolvisuals.minigame.GameKind;

public final class GridBoard {
    private static final int[][] DIRECTIONS = {{1,0},{0,1},{1,1},{1,-1}};
    private static final int[] TIC_ORDER = {4,0,2,6,8,1,3,5,7}, FOUR_ORDER = {3,2,4,1,5,0,6};
    public final GameKind kind;
    public final int width, height, turn, winner, moves;
    private final byte[] cells;

    public GridBoard(GameKind kind) {
        this(kind, new byte[kind == GameKind.TIC_TAC_TOE ? 9 : 42], 0);
    }

    private GridBoard(GameKind kind, byte[] cells, int moves) {
        if (kind == GameKind.ANGRY_BIRDS) throw new IllegalArgumentException("Not a grid game");
        this.kind = kind; this.width = kind == GameKind.TIC_TAC_TOE ? 3 : 7; this.height = cells.length / width;
        this.cells = cells; this.moves = moves; this.turn = moves % 2 + 1; this.winner = result();
    }

    public int cell(int x, int y) { return cells[y * width + x]; }

    public boolean legal(int move) {
        return winner == 0 && move >= 0 && move < (kind == GameKind.TIC_TAC_TOE ? 9 : width)
                && cells[move] == 0;
    }

    public GridBoard play(int move) {
        if (!legal(move)) throw new IllegalArgumentException("Illegal grid move");
        byte[] next = cells.clone();
        int index = move;
        if (kind == GameKind.CONNECT_FOUR) while (index + width < next.length && next[index + width] == 0) index += width;
        next[index] = (byte)turn;
        return new GridBoard(kind, next, moves + 1);
    }

    private int result() {
        int length = kind == GameKind.TIC_TAC_TOE ? 3 : 4;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int value = cell(x, y); if (value == 0) continue;
            for (int[] direction : DIRECTIONS) {
                int endX = x + (length - 1) * direction[0], endY = y + (length - 1) * direction[1];
                if (endX < 0 || endX >= width || endY < 0 || endY >= height) continue;
                int count = 1;
                while (count < length && cell(x + count * direction[0], y + count * direction[1]) == value) count++;
                if (count == length) return value;
            }
        }
        return moves == cells.length ? 3 : 0;
    }

    public int bestMove() {
        if (winner != 0) return -1;
        int best = -1, score = Integer.MIN_VALUE;
        for (int move : order()) if (legal(move)) {
            int value = -play(move).search(kind == GameKind.TIC_TAC_TOE ? 9 : 4, -100_000, 100_000);
            if (value > score) { score = value; best = move; }
        }
        return best;
    }

    private int search(int depth, int alpha, int beta) {
        if (winner != 0) return winner == 3 ? 0 : winner == turn ? 10_000 + depth : -10_000 - depth;
        if (depth == 0) return heuristic();
        for (int move : order()) if (legal(move)) {
            alpha = Math.max(alpha, -play(move).search(depth - 1, -beta, -alpha));
            if (alpha >= beta) break;
        }
        return alpha;
    }

    private int heuristic() {
        int value = 0;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int cell = cell(x, y); if (cell != 0) value += (cell == turn ? 1 : -1) * (4 - Math.abs(width / 2 - x));
            for (int[] direction : DIRECTIONS) {
                int endX = x + 3 * direction[0], endY = y + 3 * direction[1];
                if (endX < 0 || endX >= width || endY < 0 || endY >= height) continue;
                int own = 0, other = 0;
                for (int i = 0; i < 4; i++) {
                    int piece = cell(x + i * direction[0], y + i * direction[1]);
                    if (piece == turn) own++; else if (piece != 0) other++;
                }
                if (other == 0) value += own * own * 5;
                if (own == 0) value -= other * other * 5;
            }
        }
        return value;
    }

    private int[] order() { return kind == GameKind.TIC_TAC_TOE ? TIC_ORDER : FOUR_ORDER; }

    @Override public boolean equals(Object other) {
        return other instanceof GridBoard board && kind == board.kind && Arrays.equals(cells, board.cells);
    }
    @Override public int hashCode() { return 31 * kind.hashCode() + Arrays.hashCode(cells); }
}
