package com.viaversion.viafabricplus.injection.mixin.features.v26_2.block;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlock.class)
public abstract class MixinSkullBlock {

    @Shadow
    protected abstract VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context);

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void dontOutlineDragonHeadsSeparately(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            cir.setReturnValue(this.getCollisionShape(state, level, pos, context));
        }
    }

}
