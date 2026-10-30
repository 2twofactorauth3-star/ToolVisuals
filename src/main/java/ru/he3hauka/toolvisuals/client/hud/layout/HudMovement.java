package ru.he3hauka.toolvisuals.client.hud.layout;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.input.KeyEvent;

public final class HudMovement {
    private HudMovement() {}
    public static boolean active(Minecraft client) {
        return client.gui.screen() instanceof HudEditorScreen&&client.player!=null&&client.level!=null
                &&client.gui.overlay()==null&&client.isWindowActive()&&!client.isPaused()
                &&!client.player.isDeadOrDying()&&!client.player.isSleeping();
    }
    public static boolean down(Minecraft client,KeyMapping mapping) {
        if(!active(client)||mapping.isUnbound())return false;
        var key=InputConstants.getKey(mapping.saveString());
        if(key.getType()==InputConstants.Type.KEYBOARD)return InputConstants.isKeyDown(key.getValue());
        if(key.getType()==InputConstants.Type.MOUSE)return switch(key.getValue()) {
            case InputConstants.MOUSE_BUTTON_LEFT -> client.mouseHandler.isLeftPressed();
            case InputConstants.MOUSE_BUTTON_MIDDLE -> client.mouseHandler.isMiddlePressed();
            case InputConstants.MOUSE_BUTTON_RIGHT -> client.mouseHandler.isRightPressed();
            default -> false;
        };
        return false;
    }
    public static boolean movementKey(Options options,KeyEvent event) {
        return options.keyUp.matches(event)||options.keyDown.matches(event)||options.keyLeft.matches(event)
                ||options.keyRight.matches(event)||options.keyJump.matches(event)||options.keyShift.matches(event)||options.keySprint.matches(event);
    }
}
