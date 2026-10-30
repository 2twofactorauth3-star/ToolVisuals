package com.viaversion.viafabricplus.injection.mixin.features.entity.riding_offset;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.viaversion.viafabricplus.features.entity.riding_offset.EntityRidingOffsetsPre1_20_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoat.class)
public abstract class MixinAbstractBoat extends VehicleEntity {

    public MixinAbstractBoat(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "positionRider", at = @At(value = "HEAD"), cancellable = true)
    private void updatePassengerPosition1_8(Entity passenger, Entity.MoveFunction positionUpdater, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            final Vec3 newPosition = EntityRidingOffsetsPre1_20_2.getMountedHeightOffset(this, passenger).add(this.position());
            positionUpdater.accept(passenger, newPosition.x, newPosition.y + EntityRidingOffsetsPre1_20_2.getHeightOffset(passenger), newPosition.z);
            ci.cancel();
        }
    }

}
