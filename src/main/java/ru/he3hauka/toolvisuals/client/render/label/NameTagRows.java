package ru.he3hauka.toolvisuals.client.render.label;

public final class NameTagRows {
    public static final double ROW_HEIGHT = .3;
    public static final double SCORE_HEIGHT = 9 * 1.15 * .025;

    private NameTagRows() {}

    public static double above(boolean score, int row) {
        return ROW_HEIGHT * row + (score ? SCORE_HEIGHT : 0);
    }
}
