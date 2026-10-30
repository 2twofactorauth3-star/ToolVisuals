package ru.he3hauka.toolvisuals.client.optimization.equipment;

import net.minecraft.world.item.ItemStack;

public final class PreparedEquipment {
    private static final ThreadLocal<EquipmentSnapshots> SNAPSHOTS = ThreadLocal.withInitial(EquipmentSnapshots::new);

    private PreparedEquipment() {}
    public static ItemStack copy(ItemStack source) {
        return SNAPSHOTS.get().snapshot(source);
    }
    public static void clear() { SNAPSHOTS.remove(); }
}
