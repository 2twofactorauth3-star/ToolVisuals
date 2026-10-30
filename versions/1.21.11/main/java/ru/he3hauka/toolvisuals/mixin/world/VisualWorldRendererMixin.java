package ru.he3hauka.toolvisuals.mixin.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.world.VisualWorld;

@Mixin(LevelRenderer.class)
public abstract class VisualWorldRendererMixin {
    @Inject(method="submitEntities",at=@At("TAIL"))
    private void toolvisuals$world(PoseStack pose,LevelRenderState state,SubmitNodeCollector nodes,CallbackInfo callback){VisualWorld.submit(state,nodes);}
}
