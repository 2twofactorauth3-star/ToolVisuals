package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$hideBlockEntity(BlockEntity entity, float partialTick,
            ModelFeatureRenderer.CrumblingOverlay breaking,
            CallbackInfoReturnable<BlockEntityRenderState> cir) {
        ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.count(ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Counter.BLOCK_ENTITY_CANDIDATES,1);
        if (NoRender.hidesBlockEntity(entity)||ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService.hidden(entity,false)) cir.setReturnValue(null);
    }
}

