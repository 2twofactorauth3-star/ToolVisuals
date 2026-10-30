package ru.he3hauka.toolvisuals.mixin.replay;

import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets="net.minecraft.client.particle.ParticleEngine$MutableSpriteSet")
public interface ReplaySpriteAccessor {
    @Accessor("sprites") List<TextureAtlasSprite> toolvisuals$sprites();
    @Invoker("rebind") void toolvisuals$bindSprites(List<TextureAtlasSprite> sprites);
}
