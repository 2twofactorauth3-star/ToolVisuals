package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.injection.ViaFabricPlusMixinPlugin;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCauldronBlock.class)
public abstract class MixinAbstractCauldronBlock extends Block {
   @Unique
   private static final VoxelShape viaFabricPlus$collision_shape_r1_12_2_bedrock = Shapes.or(
      Shapes.box(0.0, 0.0, 0.0, 1.0, 0.3125, 1.0),
      new VoxelShape[]{
         Shapes.box(0.0, 0.0, 0.0, 0.125, 1.0, 1.0),
         Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 0.125),
         Shapes.box(0.875, 0.0, 0.0, 1.0, 1.0, 1.0),
         Shapes.box(0.0, 0.0, 0.875, 1.0, 1.0, 1.0)
      }
   );
   @Unique
   private boolean viaFabricPlus$requireOriginalShape;

   public MixinAbstractCauldronBlock(Properties properties) {
      super(properties);
   }

   @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
   private void changeOutlineShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4, CallbackInfoReturnable<VoxelShape> var5) {
      if (ViaFabricPlusMixinPlugin.MORE_CULLING_PRESENT && this.viaFabricPlus$requireOriginalShape) {
         this.viaFabricPlus$requireOriginalShape = false;
      } else if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
         var5.setReturnValue(Shapes.block());
      } else {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
      }
   }

   protected VoxelShape getCollisionShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4) {
      if (!ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
         return super.getCollisionShape(var1, var2, var3, var4);
      } else {
         return viaFabricPlus$collision_shape_r1_12_2_bedrock;
      }
   }

   public VoxelShape getOcclusionShape(BlockState state) {
      this.viaFabricPlus$requireOriginalShape = true;
      return super.getOcclusionShape(state);
   }
}
