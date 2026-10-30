package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MooshroomEntity.class)
public abstract class MixinMooshroomEntity extends AnimalEntity {

    protected MixinMooshroomEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/MooshroomEntity;getStewEffectFrom(Lnet/minecraft/item/ItemStack;)Ljava/util/Optional;"), cancellable = true)
    private void checkForItemTags(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_2)) {
            final ItemStack itemStack = player.getStackInHand(hand);
            if (!itemStack.isIn(ItemTags.SMALL_FLOWERS)) {
                cir.setReturnValue(super.interactMob(player, hand));
            }
        }
    }

    @Redirect(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/CowEntity;interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", ordinal = 0))
    private ActionResult directPass(CowEntity instance, PlayerEntity player, Hand hand) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_2)) {
            return ActionResult.PASS;
        } else {
            return super.interactMob(player, hand);
        }
    }

}
