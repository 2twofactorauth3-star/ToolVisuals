package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.render.ItemGlowQuad;

@Mixin(BlitRenderState.class)
public abstract class ItemGlowBlitMixin implements ItemGlowQuad.State {
    @Unique private ItemGlowQuad toolvisuals$glow;
    @Override public void toolvisuals$itemGlowQuad(ItemGlowQuad quad) { toolvisuals$glow = quad; }
    @Inject(method = "buildVertices", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$glowVertices(VertexConsumer vertices, CallbackInfo callback) {
        if (toolvisuals$glow == null) return;
        toolvisuals$glow.build((BlitRenderState)(Object)this, vertices); callback.cancel();
    }
}
