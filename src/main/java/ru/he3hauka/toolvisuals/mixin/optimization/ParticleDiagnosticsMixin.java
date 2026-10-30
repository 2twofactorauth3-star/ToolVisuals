package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import org.spongepowered.asm.mixin.Mixin;

import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

import static ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.*;

@Mixin(ParticleEngine.class)
public abstract class ParticleDiagnosticsMixin {
    @WrapMethod(method="tick")
    private void toolvisuals$tick(Operation<Void> original){
        long started=Diagnostics.start(PARTICLE_TICK);
        try{original.call();}finally{Diagnostics.end(PARTICLE_TICK,started);}
    }
    @WrapMethod(method="extract")
    private void toolvisuals$extract(ParticlesRenderState state,Frustum frustum,Camera camera,float partial,Operation<Void> original){
        long started=Diagnostics.start(PARTICLE_EXTRACT);
        try{original.call(state,frustum,camera,partial);}finally{Diagnostics.end(PARTICLE_EXTRACT,started);}
    }
}
