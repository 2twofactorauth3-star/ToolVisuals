package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractHorse.class)
public abstract class MixinAbstractHorse {

    @Shadow
    protected abstract boolean handleEating(Player player, ItemStack item);

    @Redirect(method = "fedFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;handleEating(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean decrementFoodItemClientside(AbstractHorse instance, Player player, ItemStack item) {
        return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_2) || this.handleEating(player, item);
    }

}
