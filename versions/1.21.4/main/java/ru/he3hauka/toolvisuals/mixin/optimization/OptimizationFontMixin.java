package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.text.PreparedTextCache;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_PREPARED_TEXT;

@Mixin(Font.class)
public abstract class OptimizationFontMixin {
    @Unique private final PreparedTextCache toolvisuals$cache=new PreparedTextCache();
    @WrapMethod(method="width(Ljava/lang/String;)I")
    private int toolvisuals$reuseMeasurement(String text,Operation<Integer> original){String protectedText=ru.he3hauka.toolvisuals.client.nameprotect.NameProtect.text(text);if(!Optimizations.enabled(OPT_PREPARED_TEXT)||protectedText.length()>2048)return original.call(protectedText);return toolvisuals$cache.width(protectedText,()->original.call(protectedText));}
}
