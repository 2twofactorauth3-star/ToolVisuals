package ru.he3hauka.toolvisuals.mixin.armor;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.armor.render.ArmorRenderContext;

@Mixin(LivingEntityRenderer.class)
public abstract class ArmorContextMixin {
    @WrapMethod(method="render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void toolvisuals$context(LivingEntityRenderState state,PoseStack pose,MultiBufferSource buffers,int light,Operation<Void> original){var previous=ArmorRenderContext.enter(state);try{original.call(state,pose,buffers,light);}finally{ArmorRenderContext.restore(previous);}}
}
