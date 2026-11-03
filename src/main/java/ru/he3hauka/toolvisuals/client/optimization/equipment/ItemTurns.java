package ru.he3hauka.toolvisuals.client.optimization.equipment;

import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public final class ItemTurns {
    private static final ThreadLocal<Quaternionf> TURNS = ThreadLocal.withInitial(Quaternionf::new);

    private ItemTurns() {}

    public static Quaternionf degrees(Axis axis, float degrees) {
        float radians = degrees * Mth.DEG_TO_RAD;
        if (axis == Axis.XP) return TURNS.get().rotationX(radians);
        if (axis == Axis.XN) return TURNS.get().rotationX(-radians);
        if (axis == Axis.YP) return TURNS.get().rotationY(radians);
        if (axis == Axis.YN) return TURNS.get().rotationY(-radians);
        if (axis == Axis.ZP) return TURNS.get().rotationZ(radians);
        if (axis == Axis.ZN) return TURNS.get().rotationZ(-radians);
        return null;
    }
}
