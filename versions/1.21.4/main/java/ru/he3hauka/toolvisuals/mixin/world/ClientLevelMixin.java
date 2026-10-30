package ru.he3hauka.toolvisuals.mixin.world;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.LIGHTNING_FLASH;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method="getSkyColor",at=@At("RETURN"),cancellable=true)
    private void toolvisuals$sky(CallbackInfoReturnable<Integer> callback){var config=ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG;if(!config.active(ru.he3hauka.toolvisuals.client.module.VisualModule.WORLD_CUSTOMIZER))return;int time=(int)config.value(ru.he3hauka.toolvisuals.client.module.VisualSetting.TIME);int tint=(int)config.value(ru.he3hauka.toolvisuals.client.module.VisualSetting.SKY_TINT);if(tint>0){int rgb=config.color(ru.he3hauka.toolvisuals.client.module.VisualSetting.SKY_TINT);float factor=time==3?.15f:.75f;callback.setReturnValue(Math.round((rgb>>16&255)*factor)<<16|Math.round((rgb>>8&255)*factor)<<8|Math.round((rgb&255)*factor));}else if(time!=0)callback.setReturnValue(time==3?0x060917:time==2?0xad5266:0x6ba6ff);}
    @Inject(method = "getSkyFlashTime", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$hideLightningFlash(CallbackInfoReturnable<Integer> cir) {
        if (NoRender.hides(LIGHTNING_FLASH)) cir.setReturnValue(0);
    }
}

