package ru.he3hauka.toolvisuals.mixin.dropprotect;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.items.dropprotect.DropProtect;

@Mixin(LocalPlayer.class)
public abstract class DropGameModeMixin {
    @Inject(method="drop",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$protect(boolean entire,CallbackInfoReturnable<Boolean> callback){if(DropProtect.world((LocalPlayer)(Object)this,entire))callback.setReturnValue(false);}
}
