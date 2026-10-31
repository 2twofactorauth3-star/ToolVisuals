package ru.he3hauka.toolvisuals.mixin.replay;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
public interface ReplayDayTimeAccessor {
    @Accessor("tickDayTime")
    boolean toolvisuals$replayTickDayTime();
}
