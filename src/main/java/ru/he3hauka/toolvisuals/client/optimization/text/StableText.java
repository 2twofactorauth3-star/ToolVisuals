 
 
package ru.he3hauka.toolvisuals.client.optimization.text;

import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

public final class StableText implements FormattedCharSequence {
    public final FormattedCharSequence text;
    public final boolean cacheable;
    public StableText(FormattedCharSequence text) {
        this.text = text;
        cacheable = text.accept((index,style,cp) -> !style.isObfuscated());
    }
    @Override public boolean accept(FormattedCharSink sink) { return text.accept(sink); }
}
