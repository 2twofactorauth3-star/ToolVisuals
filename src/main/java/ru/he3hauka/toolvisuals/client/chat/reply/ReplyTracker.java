package ru.he3hauka.toolvisuals.client.chat.reply;

import java.util.LinkedHashMap;
import java.util.UUID;

public final class ReplyTracker {
    private static final long MAX_AGE = 3_000_000_000L, REFILL = 2_000_000_000L;
    private record Hit(UUID attacker, String name, long time) {}
    public static final class Reply {
        public final boolean win;
        public final String player;
        private Reply(boolean win, String player) { this.win = win; this.player = player; }
    }
    private final LinkedHashMap<UUID, Hit> hits = new LinkedHashMap<>();
    private long refill;
    private boolean clock;
    private int tokens = 3;
    public void clear() { hits.clear(); tokens = 3; clock = false; }
    public void forget(UUID victim) { hits.remove(victim); }
    public void damage(UUID victim, UUID attacker, String name, long now) {
        forget(victim);
        hits.entrySet().removeIf(entry -> now - entry.getValue().time > MAX_AGE);
        if (attacker == null || victim.equals(attacker)) return;
        if (hits.size() >= 128) hits.remove(hits.keySet().iterator().next());
        hits.put(victim, new Hit(attacker, name, now));
    }
    public Reply death(UUID victim, String name, UUID local, boolean wins, boolean losses, long now) {
        Hit hit = hits.remove(victim);
        if (hit == null || now - hit.time < 0 || now - hit.time > MAX_AGE) return null;
        boolean win = !victim.equals(local) && hit.attacker.equals(local);
        boolean lose = victim.equals(local) && !hit.attacker.equals(local);
        if (!(win && wins || lose && losses)) return null;
        if (!clock) { refill = now; clock = true; }
        if (now - refill >= REFILL) {
            long count = (now - refill) / REFILL;
            tokens = (int)Math.min(3, tokens + count); refill = now;
        }
        if (tokens == 0) return null;
        tokens--; return new Reply(win, win ? name : hit.name);
    }
}
