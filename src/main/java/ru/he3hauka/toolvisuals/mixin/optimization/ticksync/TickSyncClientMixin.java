package ru.he3hauka.toolvisuals.mixin.optimization.ticksync;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.optimization.ticksync.TickSync;

@Mixin(Minecraft.class)
public abstract class TickSyncClientMixin {
    @Inject(method="getTickTargetMillis",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$phase(float original,CallbackInfoReturnable<Float> callback){callback.setReturnValue(TickSync.duration(callback.getReturnValue()));}
}
