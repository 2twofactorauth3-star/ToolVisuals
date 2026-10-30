package com.viaversion.viafabricplus.injection.mixin.features.world.always_tick_entities;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.EntityList;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import com.viaversion.viafabricplus.injection.access.world.always_tick_entities.IEntity;
import org.spongepowered.asm.mixin.*;

@Mixin(value = ClientWorld.class, priority = 900)
public abstract class MixinClientWorld extends World {

    @Shadow
    @Final
    EntityList entityList;

    protected MixinClientWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Overwrite
    public void tickEntity(Entity entity) {
        entity.resetPosition();
        final IEntity mixinEntity = (IEntity) entity;
        if (mixinEntity.viaFabricPlus$isInLoadedChunkAndShouldTick() || entity.isSpectator()) {
            entity.age++;
            Profilers.get().push(() -> Registries.ENTITY_TYPE.getId(entity.getType()).toString());
            entity.tick();
            Profilers.get().pop();
        }
        this.viaFabricPlus$checkChunk(entity);

        if (mixinEntity.viaFabricPlus$isInLoadedChunkAndShouldTick()) {
            for (Entity entity2 : entity.getPassengerList()) {
                this.tickPassenger(entity, entity2);
            }
        }
    }

    @Overwrite
    private void tickPassenger(Entity entity, Entity passenger) {
        if (!passenger.isRemoved() && passenger.getVehicle() == entity) {
            if (passenger instanceof PlayerEntity || this.entityList.has(passenger)) {
                final IEntity mixinPassenger = (IEntity) passenger;
                passenger.resetPosition();
                if (mixinPassenger.viaFabricPlus$isInLoadedChunkAndShouldTick()) {
                    passenger.age++;
                    passenger.tickRiding();
                }
                this.viaFabricPlus$checkChunk(passenger);

                if (mixinPassenger.viaFabricPlus$isInLoadedChunkAndShouldTick()) {
                    for (Entity entity2 : passenger.getPassengerList()) {
                        this.tickPassenger(passenger, entity2);
                    }
                }
            }
        } else {
            passenger.stopRiding();
        }
    }

    @Unique
    private void viaFabricPlus$checkChunk(Entity entity) {
        final IEntity mixinEntity = (IEntity) entity;
        final int chunkX = MathHelper.floor(entity.getX() / 16.0D);
        final int chunkZ = MathHelper.floor(entity.getZ() / 16.0D);
        if (!mixinEntity.viaFabricPlus$isInLoadedChunkAndShouldTick() || entity.getChunkPos().x != chunkX || entity.getChunkPos().z != chunkZ) {
            if (!(this.getChunk(chunkX, chunkZ).isEmpty())) {
                mixinEntity.viaFabricPlus$setInLoadedChunkAndShouldTick(true);
            }
        }
    }

}
