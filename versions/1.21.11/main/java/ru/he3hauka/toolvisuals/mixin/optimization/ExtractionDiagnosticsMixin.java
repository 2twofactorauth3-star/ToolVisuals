package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import static ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.*;

@Mixin(LevelRenderer.class)
public abstract class ExtractionDiagnosticsMixin {
    @WrapMethod(method="extractVisibleEntities")
    private void toolvisuals$entities(Camera camera,Frustum frustum,DeltaTracker delta,LevelRenderState state,Operation<Void> original){long began=Diagnostics.start(ENTITY_EXTRACT);try{original.call(camera,frustum,delta,state);}finally{Diagnostics.end(ENTITY_EXTRACT,began);}}
    @WrapMethod(method="extractVisibleBlockEntities")
    private void toolvisuals$blocks(Camera camera,float partial,LevelRenderState state,Operation<Void> original){long began=Diagnostics.start(BLOCK_ENTITY_EXTRACT);try{original.call(camera,partial,state);}finally{Diagnostics.end(BLOCK_ENTITY_EXTRACT,began);}}
}
