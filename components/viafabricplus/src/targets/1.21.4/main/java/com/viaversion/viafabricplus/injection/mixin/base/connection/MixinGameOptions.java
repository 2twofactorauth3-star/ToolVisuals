package com.viaversion.viafabricplus.injection.mixin.base.connection;

import net.minecraft.client.option.GameOptions;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameOptions.class)
public abstract class MixinGameOptions {

    @Shadow
    public boolean useNativeTransport;

    @Overwrite
    public boolean shouldUseNativeTransport() {
        if (!this.useNativeTransport) {
            ViaFabricPlusImpl.INSTANCE.logger().error("Native transport is disabled, but enabling it anyway since we use it as an indicator if the client wants to ping a server or connect to a server.");
        }
        return true;
    }

}
