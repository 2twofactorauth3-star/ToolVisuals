package ru.he3hauka.toolvisuals.mixin.session;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.session.SessionStats;

@Mixin(MultiPlayerGameMode.class)
public abstract class SessionStatsMixin {
    @Inject(method="destroyBlock",at=@At("RETURN"))
    private void toolvisuals$countBlock(BlockPos position,CallbackInfoReturnable<Boolean> result){if(result.getReturnValueZ())SessionStats.broken();}
}
