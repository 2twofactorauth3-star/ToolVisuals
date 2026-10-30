package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

@Mixin(ParticleEngine.class)
public abstract class ParticleDiagnosticsMixin {
    @WrapMethod(method="tick()V") private void toolvisuals$tick(Operation<Void> original){long started=Diagnostics.start(Diagnostics.Section.PARTICLE_TICK);try{original.call();}finally{Diagnostics.end(Diagnostics.Section.PARTICLE_TICK,started);}}
    @WrapMethod(method="render") private void toolvisuals$render(Camera camera,float partial,MultiBufferSource.BufferSource buffers,Operation<Void> original){long started=Diagnostics.start(Diagnostics.Section.PARTICLE_EXTRACT);try{original.call(camera,partial,buffers);}finally{Diagnostics.end(Diagnostics.Section.PARTICLE_EXTRACT,started);}}
}
