package ru.he3hauka.toolvisuals.client.presents;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import ru.he3hauka.toolvisuals.network.world.WorldContextWire.Area;

public final class PresentCatalog {
    public static final String ID = "halloween-2026-11";
    public static final List<Present> SPAWN = List.of(
            new Present(1, -9, 73, 17),
            new Present(2, -35, 69, -2),
            new Present(4, -17, 70, 32),
            new Present(5, 15, 64, 38),
            new Present(6, 44, 58, 104),
            new Present(7, 5, 45, 125),
            new Present(8, -7, 52, 178),
            new Present(9, -56, 52, 162),
            new Present(10, -77, 53, 136),
            new Present(11, -96, 70, 124),
            new Present(12, -134, 60, 97),
            new Present(13, -162, 67, 89),
            new Present(14, -192, 66, 95),
            new Present(15, -197, 67, 117),
            new Present(16, -215, 68, 97),
            new Present(17, -226, 80, 90),
            new Present(18, -245, 95, 86),
            new Present(19, -230, 105, 58),
            new Present(20, -244, 121, 50),
            new Present(21, -247, 120, 21),
            new Present(22, -222, 124, -8),
            new Present(23, -206, 97, -33),
            new Present(24, -193, 94, -36),
            new Present(25, -181, 91, -33),
            new Present(26, -189, 93, -38),
            new Present(27, -186, 103, -36),
            new Present(28, -186, 108, -45),
            new Present(29, -170, 93, -22),
            new Present(30, -163, 92, -3));
    public static final List<Present> ARENA = List.of(new Present(3, -156, 13, -303));
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
