package ru.he3hauka.toolvisuals.client.auction;

import java.util.ArrayList;
import java.util.List;

public final class AuctionRecording {
    public final AuctionPlan base;
    private final List<AuctionPlan.Step> steps = new ArrayList<>();
    private long lastAction;
    public AuctionRecording(AuctionPlan base) { this.base = base; }
    public int clicks() { return Math.max(0,steps.size()-1); }
    public void command(String command, long now) {
        if (clicks() > 0) throw new IllegalStateException("The opening command must precede all clicks");
        steps.clear(); steps.add(AuctionPlan.Step.command(command)); lastAction = now;
    }
    public void click(AuctionPlan.MenuKey menu, AuctionPlan.ButtonKey item, int slot, int button, long now) {
        if (steps.isEmpty()) throw new IllegalStateException("Enter the auction command first");
        if (steps.size() >= AuctionPlan.MAX_STEPS) throw new IllegalArgumentException("Recording limit reached");
        steps.add(AuctionPlan.Step.click(menu,item,slot,button,(int)Math.clamp(now-lastAction,250,10000))); lastAction = now;
    }
    public AuctionPlan finish(String id) {
        if (clicks() == 0) throw new IllegalStateException("Record at least one auction menu click");
        return base.withRecording(id,steps);
    }
}
