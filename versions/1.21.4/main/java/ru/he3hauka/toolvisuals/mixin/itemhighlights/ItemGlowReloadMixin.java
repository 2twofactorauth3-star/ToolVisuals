package ru.he3hauka.toolvisuals.mixin.itemhighlights;

import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.highlight.render.HandItemGlow;
import ru.he3hauka.toolvisuals.client.items.highlight.render.LegacyGuiGlow;

@Mixin(ModelManager.class)
public abstract class ItemGlowReloadMixin {
    @Inject(method = "apply(Lnet/minecraft/client/resources/model/ModelManager$ReloadState;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void toolvisuals$reload(CallbackInfo callback) { LegacyGuiGlow.clear(); HandItemGlow.close(); }
}
