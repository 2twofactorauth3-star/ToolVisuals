package com.viaversion.viafabricplus.injection.mixin.base.integration.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;

import com.viaversion.viafabricplus.api.events.LoadingCycleCallback;
import com.viaversion.viafabricplus.base.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void callPostGameLoadEvent(GameConfig args, CallbackInfo ci) {
        Events.LOADING_CYCLE.invoker().onLoadCycle(LoadingCycleCallback.LoadingCycle.POST_GAME_LOAD);
    }

}
