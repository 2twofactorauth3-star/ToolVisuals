package ru.he3hauka.toolvisuals.mixin.optimization;

import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemRenderer.class)
public interface GroundItemQuadInvoker {
    @Invoker("renderQuadList") static void toolvisuals$quads(PoseStack pose,VertexConsumer vertices,List<BakedQuad> quads,int[] tints,int light,int overlay){throw new AssertionError();}
}
