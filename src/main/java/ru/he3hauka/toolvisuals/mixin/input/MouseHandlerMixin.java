package ru.he3hauka.toolvisuals.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @ModifyArgs(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void toolvisuals$zoomSensitivity(Args args) {
        float factor = ru.he3hauka.toolvisuals.client.zoom.VisualZoom.sensitivity();
        args.set(0, (double)args.get(0) * factor);
        args.set(1, (double)args.get(1) * factor);
        if (ru.he3hauka.toolvisuals.client.camera.FreeLook.turn(args.get(0), args.get(1))) {
            args.set(0, 0d); args.set(1, 0d);
        }
    }
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$openMenu(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if(action==InputConstants.PRESS&&window==client.getWindow().handle())ru.he3hauka.toolvisuals.client.items.dropprotect.DropInput.mousePressed(client,new MouseButtonEvent(0,0,button));
        if(action==InputConstants.PRESS&&window==client.getWindow().handle())ru.he3hauka.toolvisuals.client.keybinds.Keybinds.press(InputConstants.Type.MOUSE.getOrCreate(button.button()));
        if (action == InputConstants.PRESS && window == client.getWindow().handle()
                && ToolVisualsClient.canOpenMenu(client)
                && ToolVisualsClient.openMenuKey.matchesMouse(new MouseButtonEvent(0, 0, button))) {
            ToolVisualsClient.openMenu(client);
            ci.cancel();
        }
    }
}

