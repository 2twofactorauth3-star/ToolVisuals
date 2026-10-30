package com.viaversion.viafabricplus.injection.mixin.features.world.disable_sequencing;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;

import com.viaversion.viafabricplus.features.world.disable_sequencing.PendingUpdateManager1_18_2;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientWorld.class, priority = 900)
public abstract class MixinClientWorld {

    @Mutable
    @Shadow
    @Final
    private PendingUpdateManager pendingUpdateManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void removePendingUpdateManager(CallbackInfo ci) {
        if (DebugSettings.INSTANCE.disableSequencing.isEnabled()) {
            this.pendingUpdateManager = new PendingUpdateManager1_18_2();
        }
    }
}
