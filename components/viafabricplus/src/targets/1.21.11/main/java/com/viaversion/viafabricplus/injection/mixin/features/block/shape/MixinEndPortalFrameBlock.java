package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalFrameBlock.class)
public abstract class MixinEndPortalFrameBlock extends Block {
   @Shadow
   @Final
   private static VoxelShape SHAPE_EMPTY;
   @Shadow
   @Final
   public static BooleanProperty HAS_EYE;
   @Unique
   private static final VoxelShape viaFabricPlus$eye_shape_r1_12_2 = Block.box(5.0, 13.0, 5.0, 11.0, 16.0, 11.0);
   @Unique
   private static final VoxelShape viaFabricPlus$frame_with_eye_shape_r1_12_2 = Shapes.or(SHAPE_EMPTY, viaFabricPlus$eye_shape_r1_12_2);
   @Unique
   private static final VoxelShape viaFabricPlus$shape_frame_bedrock = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0);

   public MixinEndPortalFrameBlock(Properties settings) {
      super(settings);
   }

   @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
   private void changeOutlineShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4, CallbackInfoReturnable<VoxelShape> var5) {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
         var5.setReturnValue(SHAPE_EMPTY);
      } else {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_2)) {
         return state.getValue(HAS_EYE) ? viaFabricPlus$frame_with_eye_shape_r1_12_2 : SHAPE_EMPTY;
      } else {
         return super.getCollisionShape(state, world, pos, context);
      }
   }

   public VoxelShape getOcclusionShape(BlockState var1) {
      ProtocolTranslator.getTargetVersion();
      Object var10001 = null;
      boolean var10000 = false;
      return super.getOcclusionShape(var1);
   }
}
