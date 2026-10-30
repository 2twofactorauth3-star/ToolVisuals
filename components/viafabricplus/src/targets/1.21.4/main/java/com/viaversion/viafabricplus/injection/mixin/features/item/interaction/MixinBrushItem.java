package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BrushItem;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BrushItem.class)
public abstract class MixinBrushItem {

    @Redirect(method = "getHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getBlockInteractionRange()D"))
    private double modifyReachDistance(PlayerEntity instance) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_2)) {
            return 5D;
        } else {
            return instance.getBlockInteractionRange();
        }
    }

}
