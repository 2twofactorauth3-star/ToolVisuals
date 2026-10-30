package ru.he3hauka.toolvisuals.client.optimization.geometry;

 
public final class UnitCircle {
    private static final double[] X = new double[65], Z = new double[65];
    static {
        for (int i = 0; i < 64; i++) {
            double angle = i * Math.PI * 2 / 64;
            X[i] = Math.cos(angle); Z[i] = Math.sin(angle);
        }
        X[64] = X[0]; Z[64] = Z[0];
    }
    private UnitCircle() {}
    public static double x(int index, int segments) { return X[index * 64 / segments]; }
    public static double z(int index, int segments) { return Z[index * 64 / segments]; }
}
