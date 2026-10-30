package ru.he3hauka.toolvisuals.client.animation;

import net.minecraft.world.phys.Vec3;

import ru.he3hauka.toolvisuals.animation.PairEmote;

public final class PairStage {
    public final Vec3 first, second;
    public final float firstYaw, secondYaw;

    private PairStage(Vec3 first, Vec3 second, float firstYaw) {
        this.first = first; this.second = second; this.firstYaw = firstYaw; secondYaw = firstYaw + 180;
    }

    public static PairStage between(PairEmote emote, Vec3 first, Vec3 second, float fallbackYaw) {
        if (emote == null || !finite(first) || !finite(second) || !Float.isFinite(fallbackYaw) || first.distanceToSqr(second) > 18.0625) return null;
        double dx = second.x - first.x, dz = second.z - first.z, distance = Math.hypot(dx, dz);
        double angle = Math.toRadians(fallbackYaw);
        double x = distance > .0001 ? dx / distance : -Math.sin(angle), z = distance > .0001 ? dz / distance : Math.cos(angle);
        var center = first.add(second).scale(.5);
        var half = new Vec3(x * emote.spacing / 2, 0, z * emote.spacing / 2);
        return new PairStage(center.subtract(half), center.add(half), (float) Math.toDegrees(Math.atan2(z, x)) - 90);
    }

    static boolean finite(Vec3 point) { return point != null && Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z); }
}
