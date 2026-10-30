package ru.he3hauka.toolvisuals.client.waypoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.storage.BoundedJson;

public final class WaypointStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final int LIMIT = 1024;
    public static final class Point {
        public final UUID id;
        public final String context, dimension, name;
        public final double x, y, z;
        public final int color;
        public final ColorStyle style;
        public final boolean enabled, death;
        public Point(UUID id, String context, String dimension, String name, double x, double y, double z,
                     int color, boolean enabled, boolean death) {
            this(id,context,dimension,name,x,y,z,ColorStyle.solid(color),enabled,death);
        }
        public Point(UUID id, String context, String dimension, String name, double x, double y, double z,
                     ColorStyle style, boolean enabled, boolean death) {
            if (id == null || context == null || context.isBlank() || context.length() > 2048 || dimension == null
                    || !dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || name == null || name.strip().isEmpty()
                    || name.strip().length() > 40 || name.chars().anyMatch(Character::isISOControl)
                    || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                    || Math.abs(x) > 30_000_000 || Math.abs(z) > 30_000_000 || Math.abs(y) > 4096)
                throw new IllegalArgumentException("Invalid waypoint");
            this.id=id;this.context=context;this.dimension=dimension;this.name=name.strip();this.x=x;this.y=y;this.z=z;
            this.style=ColorStyle.safe(style,0xb69aff);this.color=this.style.colors.getFirst();this.enabled=enabled;this.death=death;
        }
    }
    private final ru.he3hauka.toolvisuals.client.storage.JsonFile file;
    private volatile List<Point> points = List.of();
    
    public WaypointStore(Path path) { file=new ru.he3hauka.toolvisuals.client.storage.JsonFile(path); }
    public List<Point> entries(String context) { return points.stream().filter(p->p.context.equals(context)).toList(); }
    public void put(Point point) throws IOException {
        var next=new ArrayList<>(points);next.removeIf(p->p.id.equals(point.id));
        if(next.size()>=LIMIT)throw new IllegalStateException("Waypoint limit reached");
        next.add(point);write(next);
    }
    public void remove(UUID id) throws IOException { var next=new ArrayList<>(points);next.removeIf(p->p.id.equals(id));write(next); }
    public void death(String context,String dimension,double x,double y,double z) throws IOException {
        UUID id=UUID.nameUUIDFromBytes((context+":death").getBytes(StandardCharsets.UTF_8));
        var next=new ArrayList<>(points);next.removeIf(p->p.context.equals(context)&&p.death);
        next.add(new Point(id,context,dimension,"Death",x,y,z,0xff718b,true,true));write(next);
    }
    public void load() throws IOException {
        try {
            file.read(document->{
            var saved=GSON.fromJson(document,Saved.class);
            if(saved==null||saved.version!=1||saved.points==null||saved.points.size()>LIMIT)throw new IllegalArgumentException("Invalid waypoints");
            var next=new ArrayList<Point>();var ids=new HashSet<UUID>();
            for(var p:saved.points) {
                if(p==null||!ids.add(p.id))throw new IllegalArgumentException("Duplicate waypoint");
                next.add(new Point(p.id,p.context,p.dimension,p.name,p.x,p.y,p.z,ColorStyle.safe(p.style,p.color),p.enabled,p.death));
            }
            points=List.copyOf(next);
            return Boolean.TRUE;
            });
        } catch(IOException|RuntimeException error) { throw new IOException("Unreadable waypoint file; original preserved",error); }
    }
    private void write(List<Point> next) throws IOException {
        if(next.size()>LIMIT)throw new IllegalStateException("Waypoint limit reached");
        var saved=new Saved();saved.version=1;saved.points=next;
        file.write(GSON.toJson(saved));
        points=List.copyOf(next);
    }
    private static final class Saved { int version;List<Point> points; }
}
