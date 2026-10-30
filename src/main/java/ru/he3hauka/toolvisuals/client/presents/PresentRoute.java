package ru.he3hauka.toolvisuals.client.presents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PresentRoute {
    private PresentRoute() {}

    public static List<Present> plan(double x, double y, double z, List<Present> available) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || available.size() > 30)
            throw new IllegalArgumentException("Invalid route");
        List<Present> best = List.of();
        double shortest = Double.POSITIVE_INFINITY;
        for (var first : available) {
            var remaining = new ArrayList<>(available);
            var route = new ArrayList<Present>(available.size());
            var current = first;
            while (current != null) {
                route.add(current);
                remaining.remove(current);
                Present nearest = null;
                double distance = Double.POSITIVE_INFINITY;
                for (var point : remaining) {
                    double candidate = distance(current.x() + .5, current.y() + .5, current.z() + .5, point);
                    if (candidate < distance) { distance = candidate; nearest = point; }
                }
                current = nearest;
            }
            improve(x, y, z, route);
            double length = length(x, y, z, route);
            if (length < shortest) { best = List.copyOf(route); shortest = length; }
        }
        return best;
    }

    private static void improve(double x, double y, double z, List<Present> route) {
        for (int pass = 0; pass < 60; pass++) {
            boolean changed = false;
            for (int from = 0; from < route.size() - 1; from++) {
                for (int to = from + 1; to < route.size(); to++) {
                    var previous = from == 0 ? null : route.get(from - 1);
                    var start = route.get(from);
                    var end = route.get(to);
                    var next = to + 1 < route.size() ? route.get(to + 1) : null;
                    double px = previous == null ? x : previous.x() + .5;
                    double py = previous == null ? y : previous.y() + .5;
                    double pz = previous == null ? z : previous.z() + .5;
                    double old = distance(px, py, pz, start) + (next == null ? 0 : between(end, next));
                    double replacement = distance(px, py, pz, end) + (next == null ? 0 : between(start, next));
                    if (replacement + 1e-6 < old) {
                        Collections.reverse(route.subList(from, to + 1)); changed = true;
                    }
                }
            }
            if (!changed) return;
        }
    }

    public static double length(double x, double y, double z, List<Present> route) {
        double length = 0;
        for (var point : route) {
            length += distance(x, y, z, point);
            x = point.x() + .5; y = point.y() + .5; z = point.z() + .5;
        }
        return length;
    }

    private static double between(Present from, Present to) {
        return distance(from.x() + .5, from.y() + .5, from.z() + .5, to);
    }

    private static double distance(double x, double y, double z, Present point) {
        double dx = point.x() + .5 - x, dy = (point.y() + .5 - y) * 1.6, dz = point.z() + .5 - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
