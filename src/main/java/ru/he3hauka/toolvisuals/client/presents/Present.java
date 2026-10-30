package ru.he3hauka.toolvisuals.client.presents;

public record Present(int id, int x, int y, int z) {
    public Present {
        if (id < 1 || id > 30) throw new IllegalArgumentException("Invalid present ID");
    }

    public boolean matches(int blockX, int blockY, int blockZ) {
        return x == blockX && y == blockY && z == blockZ;
    }
}
