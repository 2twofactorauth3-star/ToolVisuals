package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;

import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;

import static ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.*;

@Mixin(LevelRenderer.class)
public abstract class SubmissionDiagnosticsMixin {
    @WrapMethod(method="submitEntities")
    private void toolvisuals$entities(PoseStack pose,LevelRenderState state,SubmitNodeCollector collector,Operation<Void> original){
        long started=Diagnostics.start(ENTITY_SUBMIT);
        try{original.call(pose,state,collector);}finally{Diagnostics.end(ENTITY_SUBMIT,started);}
    }
    @WrapMethod(method="submitBlockEntities")
    private void toolvisuals$blocks(PoseStack pose,LevelRenderState state,SubmitNodeStorage collector,Operation<Void> original){
        long started=Diagnostics.start(BLOCK_ENTITY_SUBMIT);
        try{original.call(pose,state,collector);}finally{Diagnostics.end(BLOCK_ENTITY_SUBMIT,started);}
    }
}
