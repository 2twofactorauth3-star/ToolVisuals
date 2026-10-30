 
 
package ru.he3hauka.toolvisuals.mixin.optimization;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import ru.he3hauka.toolvisuals.client.optimization.text.StableText;

@Mixin(ComponentRenderUtils.class)
public abstract class OptimizationChatMixin {
    @ModifyReturnValue(method="wrapComponents",at=@At("RETURN"))
    private static List<FormattedCharSequence> toolvisuals$stableChat(List<FormattedCharSequence> lines) {
        return lines.stream().map(line -> line instanceof StableText ? line : new StableText(line)).toList();
    }
}
