package ru.he3hauka.toolvisuals.client.input.tape;

import java.util.Arrays;

public enum TapeAction {
    ATTACK("Tape attack", 1),
    USE("Tape use", 2),
    BOTH("Tape both", 3),
    FORWARD("Tape forward", 4),
    BACKWARD("Tape backward", 8),
    LEFT("Tape left", 16),
    RIGHT("Tape right", 32),
    JUMP("Tape jump", 64),
    SNEAK("Tape sneak", 128),
    SPRINT("Tape sprint", 256);

    private static final TapeAction[] ALL = values();
    public final String choice;
    public final int mask;

    TapeAction(String choice, int mask) { this.choice = choice; this.mask = mask; }

    public static TapeAction at(int index) { return ALL[Math.clamp(index, 0, ALL.length - 1)]; }
    public static String[] choices() { return Arrays.stream(ALL).map(action -> action.choice).toArray(String[]::new); }
}
