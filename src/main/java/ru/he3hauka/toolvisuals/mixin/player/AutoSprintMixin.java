package ru.he3hauka.toolvisuals.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.player.sprint.AutoSprint;

@Mixin(KeyMapping.class)
public abstract class AutoSprintMixin {
    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean toolvisuals$autoSprint(boolean original) {
        if (original) return true;
        return AutoSprint.down(Minecraft.getInstance(), (KeyMapping)(Object)this, false);
    }
}
