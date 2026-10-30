package com.viaversion.viafabricplus.injection.mixin.features.entity.riding_offset;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.features.entity.riding_offset.EntityRidingOffsetsPre1_20_2;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntity.class)
public abstract class MixinAbstractBoatEntity extends VehicleEntity {

    public MixinAbstractBoatEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "updatePassengerPosition", at = @At(value = "HEAD"), cancellable = true)
    private void updatePassengerPosition1_8(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            final Vec3d newPosition = EntityRidingOffsetsPre1_20_2.getMountedHeightOffset(this, passenger).add(this.getPos());
            positionUpdater.accept(passenger, newPosition.x, newPosition.y + EntityRidingOffsetsPre1_20_2.getHeightOffset(passenger), newPosition.z);
            ci.cancel();
        }
    }

}
