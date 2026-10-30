package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(LevelRenderer.class)
public abstract class WorldFiltersMixin {
    @Inject(method="addCloudsPass",at=@At("HEAD"),cancellable=true) private void toolvisuals$clouds(CallbackInfo callback){if(NoRender.hides(CLOUDS))callback.cancel();}
    @Inject(method="addSkyPass",at=@At("HEAD"),cancellable=true) private void toolvisuals$sky(CallbackInfo callback){if(NoRender.hides(SKY))callback.cancel();}
    @Inject(method="addParticlesPass",at=@At("HEAD"),cancellable=true) private void toolvisuals$particles(CallbackInfo callback){if(NoRender.hides(ALL_PARTICLES))callback.cancel();}
    @Inject(method="renderBlockDestroyAnimation",at=@At("HEAD"),cancellable=true) private void toolvisuals$breaking(CallbackInfo callback){if(NoRender.hides(BLOCK_BREAKING))callback.cancel();}
}
