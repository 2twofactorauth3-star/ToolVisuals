package com.viaversion.viafabricplus.injection.mixin.features.entity.riding_offset;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.PositionUpdater;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CamelEntity.class)
public abstract class MixinCamelEntity extends AbstractHorseEntity {

    public MixinCamelEntity(EntityType<? extends AbstractHorseEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void onPassengerLookAround(Entity passenger) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20) && this.getControllingPassenger() != passenger) {
            this.viaFabricPlus$clampPassengerYaw1_20_1(passenger);
        }
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);

        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20)) {
            this.viaFabricPlus$clampPassengerYaw1_20_1(passenger);
        }
    }

    @Unique
    private void viaFabricPlus$clampPassengerYaw1_20_1(final Entity passenger) {
        passenger.setBodyYaw(this.getYaw());
        final float passengerYaw = passenger.getYaw();

        final float deltaDegrees = MathHelper.wrapDegrees(passengerYaw - this.getYaw());
        final float clampedDelta = MathHelper.clamp(deltaDegrees, -160.0F, 160.0F);
        passenger.prevYaw += clampedDelta - deltaDegrees;

        final float newYaw = passengerYaw + clampedDelta - deltaDegrees;
        passenger.setYaw(newYaw);
        passenger.setHeadYaw(newYaw);
    }

}
