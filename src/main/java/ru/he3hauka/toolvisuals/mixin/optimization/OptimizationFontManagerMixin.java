 
 
package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.optimization.text.PreparedTextCache;

@Mixin(FontManager.class)
public abstract class OptimizationFontManagerMixin {
    @Inject(method={"apply","updateOptions"},at=@At("TAIL"))
    private void toolvisuals$invalidateText(CallbackInfo ci) {
        PreparedTextCache.invalidate();
        ru.he3hauka.toolvisuals.client.hud.tab.font.TabFonts.invalidate();
    }
}
