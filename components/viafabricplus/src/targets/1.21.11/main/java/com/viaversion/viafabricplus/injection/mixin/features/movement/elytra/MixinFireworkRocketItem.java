package com.viaversion.viafabricplus.injection.mixin.features.movement.elytra;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketItem.class)
public abstract class MixinFireworkRocketItem {

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isFallFlying()Z", ordinal = 0))
    private boolean disableFireworkElytraBoost(Player player) {
        return ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_11) && player.isFallFlying();
    }

}
