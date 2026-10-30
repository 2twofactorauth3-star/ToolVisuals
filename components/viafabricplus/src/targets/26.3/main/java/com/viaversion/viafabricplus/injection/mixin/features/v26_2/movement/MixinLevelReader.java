package com.viaversion.viafabricplus.injection.mixin.features.v26_2.movement;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelReader.class)
public interface MixinLevelReader {

    @Inject(method = "containsAnyLiquid", at = @At("HEAD"), cancellable = true)
    private void excludeMaxEdgeBlocks(AABB box, CallbackInfoReturnable<Boolean> cir) {
        if (ViaFabricPlus.api().targetVersion().newerThanOrEqualTo(ProtocolVersion.v26_3)) {
            return;
        }

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(box.minX); x < Mth.ceil(box.maxX); x++) {
            for (int y = Mth.floor(box.minY); y < Mth.ceil(box.maxY); y++) {
                for (int z = Mth.floor(box.minZ); z < Mth.ceil(box.maxZ); z++) {
                    if (!((LevelReader) this).getBlockState(pos.set(x, y, z)).getFluidState().isEmpty()) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }
        cir.setReturnValue(false);
    }

}
