package ru.he3hauka.toolvisuals.mixin.optimization;

import java.util.List;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

@Mixin(LevelRenderer.class)
public abstract class SubmissionDiagnosticsMixin {
    @WrapMethod(method="renderEntities")
    private void toolvisuals$entities(PoseStack pose,MultiBufferSource.BufferSource buffers,Camera camera,DeltaTracker delta,List<Entity> entities,Operation<Void> original){long started=Diagnostics.start(Diagnostics.Section.ENTITY_SUBMIT);try{original.call(pose,buffers,camera,delta,entities);}finally{Diagnostics.end(Diagnostics.Section.ENTITY_SUBMIT,started);}}
}
