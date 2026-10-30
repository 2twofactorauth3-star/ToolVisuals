package com.viaversion.viafabricplus.injection.mixin.features.swinging;

import com.viaversion.viaversion.protocols.v1_15_2to1_16.storage.InventoryTracker1_16;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = InventoryTracker1_16.class, remap = false)
public abstract class MixinInventoryTracker1_16 {

    @Overwrite
    public boolean isInventoryOpen() {
        return false;
    }

}
