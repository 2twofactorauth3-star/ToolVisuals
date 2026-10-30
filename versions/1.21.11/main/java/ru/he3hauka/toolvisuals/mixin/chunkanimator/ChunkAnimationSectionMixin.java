package ru.he3hauka.toolvisuals.mixin.chunkanimator;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.chunkanimator.ChunkAnimations;
import ru.he3hauka.toolvisuals.client.chunkanimator.SectionAnimation;

@Mixin(value=RenderSection.class,remap=false)
public abstract class ChunkAnimationSectionMixin implements SectionAnimation {
    @Shadow private int fadeTime;
    @Unique private long toolvisuals$lastAnimation;
    @Override public boolean toolvisuals$animateUpdate() {
        return ChunkAnimations.animateUpdates() && System.nanoTime()-toolvisuals$lastAnimation>=ChunkAnimations.durationMillis()*1_000_000L;
    }
    @Inject(method="setFadeTime",at=@At("TAIL"))
    private void toolvisuals$started(int time,CallbackInfo callback){if(time>=0)toolvisuals$lastAnimation=System.nanoTime();}
    @ModifyReturnValue(method="getCurrentVisibility",at=@At("RETURN"))
    private float toolvisuals$visibility(float original) {
        if(!ChunkAnimations.enabled()||fadeTime<0)return original;
        long age=System.currentTimeMillis()-((RenderSection)(Object)this).getRegion().getCreationTime()-fadeTime;
        float fraction=Math.clamp(age/(float)ChunkAnimations.durationMillis(),0,1);
        return ChunkAnimations.mode()>0?fraction*fraction*(3-2*fraction):fraction;
    }
}
