package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.ENTITY_SHADOWS;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityShadowMixin {
    @Inject(method="renderShadow",at=@At("HEAD"),cancellable=true)
    private static void toolvisuals$shadow(CallbackInfo callback){if(NoRender.hides(ENTITY_SHADOWS))callback.cancel();}
}
