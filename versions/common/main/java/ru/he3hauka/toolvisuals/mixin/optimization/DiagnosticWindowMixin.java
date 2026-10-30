package ru.he3hauka.toolvisuals.mixin.optimization;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

@Mixin(Window.class)
public abstract class DiagnosticWindowMixin {
    @Inject(method="onFocus",at=@At("HEAD")) private void toolvisuals$focus(long window,boolean focused,CallbackInfo callback){if(!focused&&Diagnostics.enabled())Diagnostics.suspend();}
    @Inject(method="onIconify",at=@At("HEAD")) private void toolvisuals$iconify(long window,boolean iconified,CallbackInfo callback){if(iconified&&Diagnostics.enabled())Diagnostics.suspend();}
}
