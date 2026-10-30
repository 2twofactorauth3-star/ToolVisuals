package ru.he3hauka.toolvisuals.client.effects.render;

import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderTypeAccessor;

public final class EffectPipeline {
    private static final RenderPipeline PIPELINE=RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$debugFilledSnippet())
            .withLocation(Identifier.parse("toolvisuals:pipeline/world_effects"))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL,false))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withCull(false).build();
    public static final RenderType TYPE=RenderTypeAccessor.toolvisuals$create("toolvisuals_world_effects",
            RenderSetup.builder(PIPELINE).sortOnUpload().withForcedSolidModelPhase().createRenderSetup());
    private EffectPipeline() {}
    public static void initialize(){RenderPipelinesAccessor.toolvisuals$register(PIPELINE);}
}
