package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

@Mixin(EntityRenderer.class)
public abstract class ExtractionDiagnosticsMixin {
    @WrapMethod(method="createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;")
    private EntityRenderState toolvisuals$extract(Entity entity,float partial,Operation<EntityRenderState> original){long started=Diagnostics.start(Diagnostics.Section.ENTITY_EXTRACT);try{return original.call(entity,partial);}finally{Diagnostics.end(Diagnostics.Section.ENTITY_EXTRACT,started);}}
}
