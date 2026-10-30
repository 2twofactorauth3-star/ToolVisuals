package com.viaversion.viafabricplus.injection.mixin.features.v1_20_5.item;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockTransformer.class)
public abstract class MixinBlockTransformer {

    @Shadow
    private static boolean playerHasBlockingItemUseIntent(UseOnContext context) {
        return false;
    }

    @Redirect(method = "transformBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/BlockTransformer;playerHasBlockingItemUseIntent(Lnet/minecraft/world/item/context/UseOnContext;)Z"))
    private boolean neverCancelStripAttempt(UseOnContext context) {
        return ViaFabricPlus.api().targetVersion().newerThan(ProtocolVersion.v1_20_5) && playerHasBlockingItemUseIntent(context);
    }

}
