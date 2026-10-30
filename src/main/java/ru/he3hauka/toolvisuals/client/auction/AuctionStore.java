package ru.he3hauka.toolvisuals.client.auction;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class AuctionStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final int LIMIT = 64;
    public static final ru.he3hauka.toolvisuals.client.storage.BoundedJson.Limits JSON_LIMITS = new ru.he3hauka.toolvisuals.client.storage.BoundedJson.Limits(16 * 1024 * 1024, 131_072);
    private final ru.he3hauka.toolvisuals.client.storage.JsonFile file;
    private Map<String,AuctionPlan> plans = Map.of();
    private Map<String,String> selected = Map.of();
    private long revision;
    
    public AuctionStore(Path path) { file = new ru.he3hauka.toolvisuals.client.storage.JsonFile(path, JSON_LIMITS); }
    public long revision() { return revision; }
    public List<AuctionPlan> plans() { return plans.values().stream().sorted(Comparator.comparing(p -> p.id,String.CASE_INSENSITIVE_ORDER)).toList(); }
    public AuctionPlan get(String id) { return id == null ? null : plans.get(id.toLowerCase(Locale.ROOT)); }
    public AuctionPlan active(String server) {
        if (server == null || server.isBlank()) return null;
        server = AuctionPlan.server(server); var plan = get(selected.get(server));
        return plan != null && plan.server.equals(server) ? plan : null;
    }
    public void put(AuctionPlan plan, boolean use) throws IOException {
        var next = new LinkedHashMap<>(plans); String key = plan.id.toLowerCase(Locale.ROOT);
        if (!next.containsKey(key) && next.size() >= LIMIT) throw new IllegalArgumentException("Profile limit reached");
        next.put(key,plan); var bindings = new HashMap<>(selected);
        bindings.entrySet().removeIf(e -> e.getValue().equalsIgnoreCase(plan.id) && !e.getKey().equals(plan.server));
        if (use) bindings.put(plan.server,plan.id);
        write(next,bindings); plans = Map.copyOf(next); selected = Map.copyOf(bindings); revision++;
    }
    public void delete(String id) throws IOException {
        var next = new LinkedHashMap<>(plans); next.remove(id.toLowerCase(Locale.ROOT));
        var bindings = new HashMap<>(selected); bindings.values().removeIf(value -> value.equalsIgnoreCase(id));
        write(next,bindings); plans = Map.copyOf(next); selected = Map.copyOf(bindings); revision++;
    }
    public void load() throws IOException {
        try {
            file.read(document->{
            var data = GSON.fromJson(document,Saved.class);
            if (data == null || data.version != 1 || data.plans == null || data.plans.size() > LIMIT) throw new IllegalArgumentException("Invalid auction configuration");
            var next = new LinkedHashMap<String,AuctionPlan>();
            for (var p : data.plans) {
                if(p.manual!=null&&p.manual.size()>AuctionPlan.MAX_STEPS)throw new IllegalArgumentException("Too many recorded steps");
                var steps = new ArrayList<AuctionPlan.Step>();
                if (p.manual != null) for (var s : p.manual) {
                    if (s.command != null && !s.command.isEmpty()) steps.add(AuctionPlan.Step.command(s.command));
                    else steps.add(AuctionPlan.Step.click(new AuctionPlan.MenuKey(s.menu.type,s.menu.title,s.menu.slots),
                            new AuctionPlan.ButtonKey(s.item.item,s.item.name),s.slot,s.button,s.waitMillis));
                }
                var safe = new AuctionPlan(p.id,p.server,p.mode,p.minSeconds,p.maxSeconds,p.commands,steps,p.successText,p.failureText);
                if (next.putIfAbsent(safe.id.toLowerCase(Locale.ROOT),safe) != null) throw new IllegalArgumentException("Duplicate auction id");
            }
            var bindings = new HashMap<String,String>();
            if (data.selected != null) data.selected.forEach((server,id) -> {
                String normalized = AuctionPlan.server(server); var plan = next.get(id.toLowerCase(Locale.ROOT));
                if (plan != null && plan.server.equals(normalized)) bindings.put(normalized,plan.id);
            });
            plans = Map.copyOf(next); selected = Map.copyOf(bindings); revision++; 
            return Boolean.TRUE;
            });
        } catch (IOException | RuntimeException error) {  throw new IOException("Invalid auction configuration; original file was preserved",error); }
    }
    private void write(Map<String,AuctionPlan> plans, Map<String,String> selected) throws IOException {
        var data = new Saved(); data.version = 1; data.plans = List.copyOf(plans.values()); data.selected = selected;
        file.write(GSON.toJson(data));
    }
    private static final class Saved { int version; List<AuctionPlan> plans; Map<String,String> selected; }
}
