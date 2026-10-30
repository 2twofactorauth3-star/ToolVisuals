package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.SwingState.class)
public interface ReplaySwingAccessor {
    @Accessor("currentSwing") void toolvisuals$replaySwing(LivingEntity.SwingDescription value);
    @Accessor("ticks") void toolvisuals$replayTicks(int value);
    @Accessor("oldAnimation") void toolvisuals$replayOldAnimation(float value);
    @Accessor("animation") void toolvisuals$replayAnimation(float value);
}
