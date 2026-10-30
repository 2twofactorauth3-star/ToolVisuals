package ru.he3hauka.toolvisuals.minigame.sling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SlingGame {
    public static final int WIDTH = 660, HEIGHT = 300, GROUND = 270, X = 70, Y = 232;
    public record Pig(double x, double y, boolean alive) {}
    public record Wood(double x, double y, double width, double height, int health) {}
    public record Point(double x, double y, double time) {}
    public record Impact(double x, double y, double time, int pig, int wood) {}
    public record Shot(Level before, Level after, List<Point> path, List<Impact> impacts, int angle, int power) {
        public Shot { path = List.copyOf(path); impacts = List.copyOf(impacts); }
        public double duration() { return path.get(path.size() - 1).time(); }
    }
    public record Level(List<Pig> pigs, List<Wood> wood, int score) {
        public Level { pigs = List.copyOf(pigs); wood = List.copyOf(wood); }
    }

    private SlingGame() {}

    public static Level level(long seed) {
        var random = new Random(seed);
        var pigs = new ArrayList<Pig>(); var wood = new ArrayList<Wood>();
        for (int i = 0; i < 3; i++) {
            double x = 365 + i * 90 + random.nextInt(25);
            pigs.add(new Pig(x + 27, GROUND - 15, true));
            wood.add(new Wood(x - 5, GROUND - 64, 14, 64, 1));
            wood.add(new Wood(x + 49, GROUND - 64, 14, 64, 1));
            wood.add(new Wood(x - 8, GROUND - 76, 75, 12, 1));
        }
        return new Level(pigs, wood, 0);
    }

    public static Shot shoot(Level level, int angle, int power) {
        if (angle < 5 || angle > 85 || power < 10 || power > 100) throw new IllegalArgumentException("Invalid sling shot");
        var pigs = new ArrayList<>(level.pigs()); var wood = new ArrayList<>(level.wood());
        var path = new ArrayList<Point>(); var impacts = new ArrayList<Impact>();
        double radians = StrictMath.toRadians(angle), x = X, y = Y, vx = StrictMath.cos(radians) * power * 4.6, vy = -StrictMath.sin(radians) * power * 4.6;
        double step = 1d / 120, floorTime = 0; int score = level.score();
        path.add(new Point(x, y, 0));
        for (int tick = 1; tick <= 720; tick++) {
            double time = tick * step; vy += 350 * step; x += vx * step; y += vy * step;
            for (int i = 0; i < pigs.size(); i++) {
                var pig = pigs.get(i);
                if (pig.alive() && StrictMath.hypot(x - pig.x(), y - pig.y()) <= 24) {
                    pigs.set(i, new Pig(pig.x(), pig.y(), false)); score += 500;
                    impacts.add(new Impact(pig.x(), pig.y(), time, i, -1)); vx *= .82;
                }
            }
            for (int i = 0; i < wood.size(); i++) {
                var block = wood.get(i);
                if (block.health() <= 0 || x < block.x() - 8 || x > block.x() + block.width() + 8
                        || y < block.y() - 8 || y > block.y() + block.height() + 8) continue;
                wood.set(i, new Wood(block.x(), block.y(), block.width(), block.height(), 0)); score += 40;
                impacts.add(new Impact(x, y, time, -1, i)); vx *= .82; vy *= .75;
                for (int p = 0; p < pigs.size(); p++) {
                    var pig = pigs.get(p);
                    if (pig.alive() && StrictMath.hypot(block.x() + block.width() / 2 - pig.x(), block.y() + block.height() - pig.y()) < 61) {
                        pigs.set(p, new Pig(pig.x(), pig.y(), false)); score += 500;
                        impacts.add(new Impact(pig.x(), pig.y(), time, p, -1));
                    }
                }
            }
            if (y >= GROUND - 9) {
                y = GROUND - 9; vy = -Math.abs(vy) * .28; vx *= .9; floorTime += step;
            }
            if (tick % 2 == 0) path.add(new Point(x, y, time));
            if (x > WIDTH + 20 || x < -20 || floorTime > .35) { path.add(new Point(x, y, time)); break; }
        }
        return new Shot(level, new Level(pigs, wood, score), path, impacts, angle, power);
    }

    public static int[] aim(Level level, long seed) {
        var random = new Random(seed); int bestScore = -1, angle = 40, power = 80;
        for (int i = 0; i < 24; i++) {
            int a = 20 + random.nextInt(56), p = 66 + random.nextInt(35);
            int score = shoot(level, a, p).after().score();
            if (score > bestScore) { bestScore = score; angle = a; power = p; }
        }
        return new int[]{angle, power};
    }
}
