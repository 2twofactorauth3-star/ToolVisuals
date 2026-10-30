package com.viaversion.viafabricplus.injection.mixin.base.connection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.screen.multiplayer.ConnectScreen$1")
public abstract class MixinConnectScreen_1 {

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Ljava/lang/Exception;getMessage()Ljava/lang/String;", remap = false))
    private String handleNullExceptionMessage(Exception instance, Operation<String> original) {

        return instance.getMessage() == null ? "" : original.call(instance);
    }

}
