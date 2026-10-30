package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;

import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

import static ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.*;

@Mixin(LevelExtractor.class)
public abstract class ExtractionDiagnosticsMixin {
    @WrapMethod(method="extract")
    private void toolvisuals$extract(DeltaTracker delta,Camera camera,float partial,Operation<Void> original){
        long started=Diagnostics.start(VANILLA_EXTRACT);
        try{original.call(delta,camera,partial);}finally{Diagnostics.end(VANILLA_EXTRACT,started);}
    }
    @WrapMethod(method="extractVisibleEntities")
    private void toolvisuals$entities(Camera camera,Frustum frustum,DeltaTracker delta,LevelRenderState state,Operation<Void> original){
        long started=Diagnostics.start(ENTITY_EXTRACT);
        try{original.call(camera,frustum,delta,state);}finally{Diagnostics.end(ENTITY_EXTRACT,started);}
    }
    @WrapMethod(method="extractVisibleBlockEntities")
    private void toolvisuals$blocks(Camera camera,float partial,LevelRenderState state,Operation<Void> original){
        long started=Diagnostics.start(BLOCK_ENTITY_EXTRACT);
        try{original.call(camera,partial,state);}finally{Diagnostics.end(BLOCK_ENTITY_EXTRACT,started);}
    }
}
