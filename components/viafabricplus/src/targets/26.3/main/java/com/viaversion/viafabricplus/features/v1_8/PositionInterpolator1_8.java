package com.viaversion.viafabricplus.features.v1_8;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.injection.access.v1_8.IAbstractBoat;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.world.entity.AbstractInterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public final class PositionInterpolator1_8 extends AbstractInterpolationHandler {

    private final AbstractBoat boatEntity;
    private final PositionAndRotation.Mutable interpolationData = new PositionAndRotation.Mutable();

    public PositionInterpolator1_8(final AbstractBoat entity) {
        super(entity, LinearInterpolationHandler.DEFAULT_INTERPOLATION_STEPS);
        this.boatEntity = entity;
    }

    @Override
    public PositionAndRotation.@NonNull Mutable interpolationData() {
        return this.interpolationData;
    }

    @Override
    protected void interpolateTo(final PositionPath position, final float yaw, final float pitch) {
        final Vec3 pos = position.endPosition();
        final IAbstractBoat mixinBoatEntity = (IAbstractBoat) this.boatEntity;
        if (  boatEntity.isVehicle() && ViaFabricPlus.api().targetVersion().newerThan(ProtocolVersion.v1_7_6)) {
            boatEntity.xo = pos.x;
            boatEntity.yo = pos.y;
            boatEntity.zo = pos.z;
            mixinBoatEntity.viaFabricPlus$setBoatInterpolationSteps(0);
            boatEntity.setPos(pos);
            boatEntity.setRot(yaw, pitch);
            boatEntity.setDeltaMovement(Vec3.ZERO);
            mixinBoatEntity.viaFabricPlus$setBoatVelocity(Vec3.ZERO);
        } else {
            if (!boatEntity.isVehicle()) {
                mixinBoatEntity.viaFabricPlus$setBoatInterpolationSteps(8);
            } else {
                if (boatEntity.distanceToSqr(pos.x, pos.y, pos.z) <= 1) {
                    return;
                }
                mixinBoatEntity.viaFabricPlus$setBoatInterpolationSteps(3);
            }

            this.interpolationData.set(pos, yaw, pitch);
            boatEntity.setDeltaMovement(mixinBoatEntity.viaFabricPlus$getBoatVelocity());
        }
    }

    @Override
    protected void startInterpolating(final @NonNull PositionPath position, final float yRot, final float xRot) {
    }

    @Override
    protected void doInterpolate() {
    }

    @Override
    public void interpolate() {
    }

    @Override
    public boolean hasActiveInterpolation() {
        return false;
    }

    @Override
    public void cancel() {
    }

}
