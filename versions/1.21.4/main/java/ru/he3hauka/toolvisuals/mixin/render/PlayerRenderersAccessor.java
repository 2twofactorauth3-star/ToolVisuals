package ru.he3hauka.toolvisuals.mixin.render;

import java.util.Map;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderDispatcher.class)
public interface PlayerRenderersAccessor {
    @Accessor("playerRenderers") Map<PlayerSkin.Model,EntityRenderer<? extends Player,?>> toolvisuals$playerRenderers();
}
