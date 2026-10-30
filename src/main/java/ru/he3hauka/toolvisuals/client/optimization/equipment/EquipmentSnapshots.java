package ru.he3hauka.toolvisuals.client.optimization.equipment;

import java.util.IdentityHashMap;
import net.minecraft.world.item.ItemStack;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

public final class EquipmentSnapshots {
    private static final int LIMIT = 1024;
    private final IdentityHashMap<ItemStack, ItemStack> snapshots = new IdentityHashMap<>(LIMIT);

    public ItemStack snapshot(ItemStack source) {
        if (source.isEmpty()) return ItemStack.EMPTY;
        var previous = snapshots.get(source);
        if (previous != null && ItemStack.matches(source, previous)) {
            Diagnostics.count(Diagnostics.Counter.EQUIPMENT_CACHE_HIT, 1);
            return previous;
        }
        if (previous == null && snapshots.size() >= LIMIT) snapshots.remove(snapshots.keySet().iterator().next());
        var current = source.copy();
        snapshots.put(source, current);
        Diagnostics.count(Diagnostics.Counter.EQUIPMENT_CACHE_MISS, 1);
        return current;
    }

    public void clear() { snapshots.clear(); }
}
