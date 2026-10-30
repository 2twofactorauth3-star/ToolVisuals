package com.viaversion.viafabricplus.injection.mixin.features.world.item_picking;

import net.minecraft.client.MinecraftClient;

import com.viaversion.viafabricplus.features.world.item_picking.ItemPick1_21_3;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void pickItemClientside(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_2)) {
            ItemPick1_21_3.doItemPick((MinecraftClient) (Object) this);
            ci.cancel();
        }
    }

}
