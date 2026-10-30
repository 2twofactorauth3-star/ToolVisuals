package com.viaversion.viafabricplus.injection.mixin.base.integration.event;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;

import com.viaversion.viafabricplus.api.events.LoadingCycleCallback;
import com.viaversion.viafabricplus.base.Events;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void callPostGameLoadEvent(RunArgs args, CallbackInfo ci) {
        Events.LOADING_CYCLE.invoker().onLoadCycle(LoadingCycleCallback.LoadingCycle.POST_GAME_LOAD);
    }

}
