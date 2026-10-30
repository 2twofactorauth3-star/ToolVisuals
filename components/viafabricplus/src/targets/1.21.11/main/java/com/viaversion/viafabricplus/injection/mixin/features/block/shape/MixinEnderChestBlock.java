package com.viaversion.viafabricplus.injection.mixin.features.block.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderChestBlock.class)
public abstract class MixinEnderChestBlock extends BaseEntityBlock {
   @Unique
   private static final VoxelShape viaFabricPlus$shape_bedrock = Shapes.box(0.025, 0.0, 0.025, 0.975, 0.95, 0.975);
   @Shadow
   @Final
   private static VoxelShape SHAPE;

   protected MixinEnderChestBlock(Properties settings) {
      super(settings);
   }

   @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
   private void changeOutlineShape(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4, CallbackInfoReturnable<VoxelShape> var5) {
      if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_4_2)) {
         var5.setReturnValue(Shapes.block());
      } else {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
      }
   }

   public VoxelShape getOcclusionShape(BlockState var1) {
      if (!ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_4_2)) {
         ProtocolTranslator.getTargetVersion();
         Object var10001 = null;
         boolean var10000 = false;
         return super.getOcclusionShape(var1);
      } else {
         return SHAPE;
      }
   }
}
