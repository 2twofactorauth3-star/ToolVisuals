package ru.he3hauka.toolvisuals.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$openMenu(long window, int action, KeyEvent event, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if(action==InputConstants.PRESS&&window==client.getWindow().handle())ru.he3hauka.toolvisuals.client.items.dropprotect.DropInput.keyPressed(client,event);
        if(action==InputConstants.PRESS&&window==client.getWindow().handle())ru.he3hauka.toolvisuals.client.keybinds.Keybinds.press(InputConstants.getKey(event));
        if(action==InputConstants.PRESS&&window==client.getWindow().handle()&&client.gui.screen() instanceof ru.he3hauka.toolvisuals.client.animation.ui.EmoteWheelScreen wheel&&event.key()>=InputConstants.KEY_F1&&event.key()<=InputConstants.KEY_F12){wheel.keyPressed(event);ci.cancel();return;}
        if (action == InputConstants.PRESS && window == client.getWindow().handle()
                && ToolVisualsClient.canOpenMenu(client) && ToolVisualsClient.openMenuKey.matches(event)) {
            ToolVisualsClient.openMenu(client);
            ci.cancel();
        }
    }
}

