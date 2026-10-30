package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.WORLD_CUSTOMIZER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.TIME;

@Mixin(Level.class)
public abstract class WorldTimeMixin {
    @Inject(method="getDayTime",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$localTime(CallbackInfoReturnable<Long> callback){if(!((Level)(Object)this).isClientSide||!ToolVisualsClient.CONFIG.active(WORLD_CUSTOMIZER))return;int mode=(int)ToolVisualsClient.CONFIG.value(TIME);if(mode!=0)callback.setReturnValue(mode==1?6000L:mode==2?12500L:18000L);}
}
