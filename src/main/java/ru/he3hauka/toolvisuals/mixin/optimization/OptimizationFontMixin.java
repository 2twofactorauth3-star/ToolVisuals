 
 
package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;
import ru.he3hauka.toolvisuals.client.optimization.*;
import ru.he3hauka.toolvisuals.client.optimization.text.PreparedTextCache;
import ru.he3hauka.toolvisuals.client.optimization.text.StableText;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_PREPARED_TEXT;

@Mixin(Font.class)
public abstract class OptimizationFontMixin {
    @Unique private final PreparedTextCache toolvisuals$prepared = new PreparedTextCache();
    @WrapMethod(method="prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;")
    private Font.PreparedText toolvisuals$reusePrepared(FormattedCharSequence text,float x,float y,int color,boolean shadow,boolean includeEmpty,int background,
                                                       Operation<Font.PreparedText> original) {
        text = NameProtect.text(text);
        if (!Optimizations.enabled(OPT_PREPARED_TEXT) || !(text instanceof StableText stable) || !stable.cacheable)
            return original.call(text,x,y,color,shadow,includeEmpty,background);
        var key = new PreparedTextCache.Key(stable,x,y,color,shadow,includeEmpty,background);
        var cached = toolvisuals$prepared.get(key);
        if (cached != null) return cached;
         
        var result = original.call(text,x,y,color,shadow,includeEmpty,background);
        toolvisuals$prepared.put(key,result); return result;
    }
}
