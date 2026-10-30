package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.GLOWING;

@Mixin(Minecraft.class)
public abstract class GlowingFilterMixin {
    @Inject(method="shouldEntityAppearGlowing",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$glowing(CallbackInfoReturnable<Boolean> callback){if(NoRender.hides(GLOWING))callback.setReturnValue(false);}
}
