 
 
package ru.he3hauka.toolvisuals.mixin.localization;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.optimization.text.StableText;

@Mixin(ClientLanguage.class)
public abstract class OptimizationLanguageMixin {
    @Inject(method="getVisualOrder",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$stableText(FormattedText text,CallbackInfoReturnable<FormattedCharSequence> cir) {
        if (text instanceof Component && !(cir.getReturnValue() instanceof StableText))
            cir.setReturnValue(new StableText(cir.getReturnValue()));
    }
}
