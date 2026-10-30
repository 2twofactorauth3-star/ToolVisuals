package ru.he3hauka.toolvisuals.animation.network;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RelayLimits {
    public static final int MAX_PLAYERS = 2048;
    public enum Delivery { SEND, SKIP, EXHAUSTED }
    private final PacketBudget requests;
    private final PacketBudget starts;
    private final PacketBudget deliveries;
    private final Map<UUID, PacketBudget> recipients = new HashMap<>();
    private final java.util.LinkedHashSet<UUID> waiting = new java.util.LinkedHashSet<>();
    private int turnUsed;
    private final int recipientBurst,recipientRate;

    public RelayLimits(long now) { this(now,1024,4096,512,512); }

    public RelayLimits(long now,int deliveryBurst,int deliveryRate,int recipientBurst,int recipientRate) {
        this.recipientBurst=recipientBurst;this.recipientRate=recipientRate;
        requests = new PacketBudget(256, 4096, now);
        starts = new PacketBudget(64, 128, now);
        deliveries = new PacketBudget(deliveryBurst, deliveryRate, now);
    }

    public synchronized boolean request(long now) { return requests.allow(now); }
    public synchronized boolean start(long now) { return starts.allow(now); }

    public synchronized Delivery delivery(UUID recipient, long now) {
        var budget = recipients.get(recipient);
        if (budget == null) {
            if (recipients.size() >= MAX_PLAYERS) return deliveries.available(now) ? Delivery.SKIP : Delivery.EXHAUSTED;
            budget = new PacketBudget(recipientBurst, recipientRate, now);
            recipients.put(recipient, budget);
        }
        if (!budget.available(now)) { idle(recipient); return Delivery.SKIP; }
        if (!waiting.isEmpty() && !waiting.iterator().next().equals(recipient) || !deliveries.available(now)) {
            if (waiting.size() < MAX_PLAYERS) waiting.add(recipient);
            return Delivery.EXHAUSTED;
        }
        if (waiting.contains(recipient) && ++turnUsed >= 32) { waiting.remove(recipient); turnUsed = 0; }
        budget.allow(now);
        deliveries.allow(now);
        return Delivery.SEND;
    }

    public synchronized void idle(UUID player) { if (!waiting.isEmpty() && waiting.iterator().next().equals(player)) turnUsed = 0; waiting.remove(player); }
    public synchronized void remove(UUID player) { recipients.remove(player); idle(player); }
    public synchronized void retain(Collection<UUID> players) { recipients.keySet().retainAll(players); if (!waiting.isEmpty() && !players.contains(waiting.iterator().next())) turnUsed = 0; waiting.retainAll(players); }
}
