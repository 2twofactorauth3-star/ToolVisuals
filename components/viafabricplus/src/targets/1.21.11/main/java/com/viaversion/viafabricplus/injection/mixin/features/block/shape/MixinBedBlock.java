package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.injection.ViaFabricPlusMixinPlugin;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class MixinBedBlock extends HorizontalDirectionalBlock {
   @Unique
   private static final VoxelShape viaFabricPlus$shape_r1_13_2 = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);
   @Unique
   private boolean viaFabricPlus$requireOriginalShape;

   protected MixinBedBlock(Properties settings) {
      super(settings);
   }

   @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
   private void changeOutlineShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4, CallbackInfoReturnable<VoxelShape> var5) {
      if (ViaFabricPlusMixinPlugin.MORE_CULLING_PRESENT && this.viaFabricPlus$requireOriginalShape) {
         this.viaFabricPlus$requireOriginalShape = false;
      } else if (!ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
      } else {
         var5.setReturnValue(viaFabricPlus$shape_r1_13_2);
      }
   }

   public VoxelShape getOcclusionShape(BlockState state) {
      this.viaFabricPlus$requireOriginalShape = true;
      return super.getOcclusionShape(state);
   }
}
