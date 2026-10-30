package com.viaversion.viafabricplus.injection.mixin.features.entity.interaction;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractBoatEntity.class)
public abstract class MixinAbstractBoatEntity extends VehicleEntity {

    public MixinAbstractBoatEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/VehicleEntity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult makeNotLeashable(VehicleEntity instance, PlayerEntity player, Hand hand) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_5)) {
            return ActionResult.PASS;
        } else {
            return super.interact(player, hand);
        }
    }

}
