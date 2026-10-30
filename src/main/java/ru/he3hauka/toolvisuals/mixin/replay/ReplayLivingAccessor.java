package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface ReplayLivingAccessor {
    @Accessor("swingState") LivingEntity.SwingState toolvisuals$replaySwing();
}
