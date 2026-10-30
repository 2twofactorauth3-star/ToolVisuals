package com.viaversion.viafabricplus.injection.mixin.features.world.remove_server_view_distance;

import net.minecraft.client.option.GameOptions;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameOptions.class)
public abstract class MixinGameOptions {

    @ModifyVariable(method = "setServerViewDistance", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int changeServerViewDistance(int viewDistance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_17_1)) {
            return 0;
        } else {
            return viewDistance;
        }
    }

}
