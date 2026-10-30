package ru.he3hauka.toolvisuals.mixin.optimization;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.terrain.BoundaryFaces;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_HIDDEN_FACES;

@Mixin(value=BlockRenderer.class,remap=false)
public abstract class HiddenFacesMixin extends AbstractBlockRenderContext {
    @Unique private static final Direction[] toolvisuals$directions=Direction.values();
    @Unique private int toolvisuals$knownNeighbors,toolvisuals$solidNeighbors;

    @Inject(method="renderModel",at=@At("HEAD"))
    private void toolvisuals$resetNeighbors(CallbackInfo callback){toolvisuals$knownNeighbors=toolvisuals$solidNeighbors=0;}

    @Inject(method="processQuad",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipHiddenFace(MutableQuadViewImpl quad,CallbackInfo callback) {
        if(!((SodiumCullingAccessor)(Object)this).toolvisuals$culling()||!Optimizations.enabled(OPT_HIDDEN_FACES)||quad.getCullFace()!=null||slice==null||state.hasOffsetFunction())return;
        int mask=BoundaryFaces.mask(quad.getX(0),quad.getY(0),quad.getZ(0),quad.getX(1),quad.getY(1),quad.getZ(1),
                quad.getX(2),quad.getY(2),quad.getZ(2),quad.getX(3),quad.getY(3),quad.getZ(3));
        if(mask==0)return;
        for(var direction:toolvisuals$directions){
            int bit=1<<direction.get3DDataValue();
            if((mask&bit)==0)continue;
            if((toolvisuals$knownNeighbors&bit)==0){
                toolvisuals$knownNeighbors|=bit;
                if(slice.getBlockState(pos.getX()+direction.getStepX(),pos.getY()+direction.getStepY(),pos.getZ()+direction.getStepZ()).isSolidRender())toolvisuals$solidNeighbors|=bit;
            }
            if((toolvisuals$solidNeighbors&bit)!=0){Diagnostics.count(Diagnostics.Counter.QUADS_SKIPPED,1);callback.cancel();return;}
        }
    }
}
