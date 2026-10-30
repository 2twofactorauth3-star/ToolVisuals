package com.viaversion.viafabricplus.injection.mixin.features.movement.elytra;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "checkGliding", at = @At("HEAD"), cancellable = true)
    private void replaceGlidingCondition(CallbackInfoReturnable<Boolean> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_14_4)) {
            if (!this.isOnGround() && this.getVelocity().y < 0D && !this.isGliding()) {
                final ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);
                if (itemStack.isOf(Items.ELYTRA) && canGlideWith(itemStack, EquipmentSlot.CHEST)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
            cir.setReturnValue(false);
        }
    }

}
