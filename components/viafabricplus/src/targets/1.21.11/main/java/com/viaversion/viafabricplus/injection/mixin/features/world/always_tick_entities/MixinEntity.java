package com.viaversion.viafabricplus.injection.mixin.features.world.always_tick_entities;

import net.minecraft.world.entity.Entity;

import com.viaversion.viafabricplus.injection.access.world.always_tick_entities.IEntity;
import com.viaversion.viafabricplus.settings.impl.DebugSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntity {

    @Unique
    private boolean viaFabricPlus$isInLoadedChunkAndShouldTick;

    @Override
    public boolean viaFabricPlus$isInLoadedChunkAndShouldTick() {
        return this.viaFabricPlus$isInLoadedChunkAndShouldTick || DebugSettings.INSTANCE.alwaysTickClientPlayer.isEnabled();
    }

    @Override
    public void viaFabricPlus$setInLoadedChunkAndShouldTick(final boolean inLoadedChunkAndShouldTick) {
        this.viaFabricPlus$isInLoadedChunkAndShouldTick = inLoadedChunkAndShouldTick;
    }

}
