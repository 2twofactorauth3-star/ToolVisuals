package ru.he3hauka.toolvisuals.mixin.appleskin;

import java.util.Vector;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import squeek.appleskin.util.IntPoint;

@Mixin(targets="squeek.appleskin.client.HUDOverlayHandler$OffsetsCache", remap=false)
public interface AppleSkinOffsetsInvoker {
    @Invoker("foodBarOffsets")
    Vector<IntPoint> toolvisuals$foodBarOffsets(int tick, Player player);
}
