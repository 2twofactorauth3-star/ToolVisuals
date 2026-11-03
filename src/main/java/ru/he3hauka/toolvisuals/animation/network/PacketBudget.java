package ru.he3hauka.toolvisuals.animation.network;

public final class PacketBudget {
    private final int capacity;
    private final double perNanosecond;
    private double available;
    private long updated;

    public PacketBudget(int capacity, int perSecond, long now) {
        if (capacity < 1 || perSecond < 1) throw new IllegalArgumentException("Invalid packet budget");
        this.capacity = capacity;
        perNanosecond = perSecond / 1_000_000_000d;
        available = capacity;
        updated = now;
    }

    public boolean available(long now) {
        long elapsed = now - updated;
        if (elapsed > 0) {
            available = Math.min(capacity, available + elapsed * perNanosecond);
            updated = now;
        }
        return available >= 1;
    }

    public boolean allow(long now) {
        return allow(now,1);
    }
    public boolean allow(long now,int cost) {
        if(!available(now,cost))return false;
        available-=cost;
        return true;
    }
    public boolean available(long now,int cost) {
        if(cost<1||cost>capacity)return false;
        available(now);
        return available>=cost;
    }
}
