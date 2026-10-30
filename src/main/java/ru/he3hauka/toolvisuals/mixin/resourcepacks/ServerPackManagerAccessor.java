package ru.he3hauka.toolvisuals.mixin.resourcepacks;

import net.minecraft.client.resources.server.ServerPackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPackManager.class)
public interface ServerPackManagerAccessor {
    @Accessor("packPromptStatus") ServerPackManager.PackPromptStatus toolvisuals$consent();
}
