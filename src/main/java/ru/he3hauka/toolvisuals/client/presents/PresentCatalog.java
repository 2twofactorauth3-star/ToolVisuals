package ru.he3hauka.toolvisuals.client.presents;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire.Area;

public final class PresentCatalog {
    public static final List<Present> SPAWN = List.of(
            new Present(1, -41, 114, -32),
            new Present(2, -29, 113, -12),
            new Present(3, -21, 113, -2),
            new Present(4, -4, 115, 7),
            new Present(5, -30, 99, 28),
            new Present(6, -31, 120, 44),
            new Present(7, -59, 105, 54),
            new Present(8, -110, 98, 91),
            new Present(9, -114, 117, 65),
            new Present(10, -101, 142, 76),
            new Present(11, -7, 100, 80),
            new Present(12, -102, 151, -18),
            new Present(13, -64, 114, -66),
            new Present(14, -152, 128, -49),
            new Present(15, 3, 104, 48),
            new Present(16, -99, 102, 83),
            new Present(17, -151, 82, -2),
            new Present(18, -22, 113, -4),
            new Present(19, -134, 154, -30),
            new Present(20, -96, 113, -12),
            new Present(21, 22, 108, 10),
            new Present(22, -45, 113, -5),
            new Present(23, -74, 115, -22),
            new Present(24, -85, 135, -45),
            new Present(25, -40, 112, -41),
            new Present(26, -163, 96, 45),
            new Present(27, -136, 98, 67),
            new Present(29, -54, 113, 1),
            new Present(30, -47, 98, 73));
    public static final List<Present> ARENA = List.of(new Present(28, -127, 40, -128));
    public static final List<Present> ALL = Stream.concat(SPAWN.stream(), ARENA.stream()).toList();

    private PresentCatalog() {}

    public static Area area(String dimension) {
        if (dimension == null) return Area.OTHER;
        int separator = dimension.indexOf(':');
        return separator > 0 ? Area.fromName(dimension.substring(separator + 1)) : Area.OTHER;
    }

    public static List<Present> points(Area area) {
        return switch (area) {
            case SPAWN -> SPAWN;
            case ARENA -> ARENA;
            case OTHER -> List.of();
        };
    }

    public static boolean spawnComplete(Set<Integer> collected) {
        return SPAWN.stream().allMatch(point -> collected.contains(point.id()));
    }

    public static List<Present> remaining(Area area, Set<Integer> collected) {
        if (area == Area.ARENA && !spawnComplete(collected)) return List.of();
        return points(area).stream().filter(point -> !collected.contains(point.id())).toList();
    }

    public static Present at(Area area, int x, int y, int z) {
        for (var point : points(area)) if (point.matches(x, y, z)) return point;
        return null;
    }
}
