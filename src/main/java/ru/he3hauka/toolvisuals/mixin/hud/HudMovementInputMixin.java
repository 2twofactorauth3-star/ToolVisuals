package ru.he3hauka.toolvisuals.mixin.hud;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.hud.layout.HudEditorScreen;
import ru.he3hauka.toolvisuals.client.hud.layout.HudMovement;
import ru.he3hauka.toolvisuals.client.player.sprint.AutoSprint;

@Mixin(KeyboardInput.class)
public abstract class HudMovementInputMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"), require = 7)
    private boolean toolvisuals$hudMovement(KeyMapping mapping, Operation<Boolean> original) {
        var client = Minecraft.getInstance();
        boolean down = client.gui.screen() instanceof HudEditorScreen ? HudMovement.down(client, mapping) : original.call(mapping);
        return AutoSprint.down(client, mapping, down);
    }
}
