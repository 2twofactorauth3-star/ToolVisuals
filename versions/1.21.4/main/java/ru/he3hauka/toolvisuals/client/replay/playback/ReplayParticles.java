package ru.he3hauka.toolvisuals.client.replay.playback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureManager;
import ru.he3hauka.toolvisuals.mixin.replay.ReplayParticleAccessor;
import ru.he3hauka.toolvisuals.mixin.replay.ReplaySpriteAccessor;

public final class ReplayParticles implements AutoCloseable {
    private final TextureManager textures;
    public final ParticleEngine engine;
    public ReplayParticles(Minecraft client){
        textures=new TextureManager(client.getResourceManager());
        engine=new ParticleEngine(null,textures);
        var live=((ReplayParticleAccessor)client.particleEngine).toolvisuals$spriteSets();
        try{
            for(var entry:((ReplayParticleAccessor)engine).toolvisuals$spriteSets().entrySet()){
                var source=live.get(entry.getKey());
                if(source==null)throw new IllegalStateException("Missing live particle sprites: "+entry.getKey());
                ((ReplaySpriteAccessor)entry.getValue()).toolvisuals$bindSprites(((ReplaySpriteAccessor)source).toolvisuals$sprites());
            }
        }catch(RuntimeException|Error failure){textures.close();throw failure;}
    }
    @Override public void close(){((ReplayParticleAccessor)engine).toolvisuals$clearReplayParticles();textures.close();}
}
