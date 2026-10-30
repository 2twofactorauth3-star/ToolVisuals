package ru.he3hauka.toolvisuals.client.pvp;

 
public final class CombatTimer {
    private long deadline;
    public void begin(long now, int seconds) { deadline = now + Math.max(0,seconds) * 1_000_000_000L; }
    public int remaining(long now) { return (int)Math.max(0,(deadline - now + 999_999_999L) / 1_000_000_000L); }
    public void clear() { deadline = 0; }
}
