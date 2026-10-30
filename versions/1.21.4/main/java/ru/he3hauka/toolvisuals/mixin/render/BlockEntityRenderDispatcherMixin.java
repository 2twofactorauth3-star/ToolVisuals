package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$filter(BlockEntity entity,float partial,com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,CallbackInfo callback){Diagnostics.count(Diagnostics.Counter.BLOCK_ENTITY_CANDIDATES,1);var renderer=((BlockEntityRenderDispatcher)(Object)this).getRenderer(entity);boolean global=renderer!=null&&renderer.shouldRenderOffScreen(entity);if(NoRender.hidesBlockEntity(entity)||OcclusionService.hidden(entity,global))callback.cancel();}
}
