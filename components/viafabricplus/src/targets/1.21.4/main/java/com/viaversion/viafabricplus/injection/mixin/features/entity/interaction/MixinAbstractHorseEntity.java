package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractHorseEntity.class)
public abstract class MixinAbstractHorseEntity {

    @Shadow
    protected abstract boolean receiveFood(PlayerEntity player, ItemStack item);

    @Redirect(method = "interactHorse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AbstractHorseEntity;receiveFood(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean decrementFoodItemClientside(AbstractHorseEntity instance, PlayerEntity player, ItemStack item) {
        return ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_2) || this.receiveFood(player, item);
    }

}
