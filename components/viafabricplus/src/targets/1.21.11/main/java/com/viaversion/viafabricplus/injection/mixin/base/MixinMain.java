package com.viaversion.viafabricplus.injection.mixin.base;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public abstract class MixinMain {

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;startTimerHackThread()V"))
    private static void bootstrap(CallbackInfo ci) {
        ViaFabricPlusImpl.INSTANCE.init();
    }

}
