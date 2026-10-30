package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderSetupAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderTypeAccessor;

public final class AccessoryPipeline {
    private static final RenderPipeline PIPELINE=RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$debugFilledSnippet())
            .withLocation(Identifier.parse("toolvisuals:pipeline/accessories"))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL,true))
            .withColorTargetState(ColorTargetState.DEFAULT).withCull(false).build();
    public static final RenderType TYPE=create();
    private AccessoryPipeline(){}
    public static void initialize(){RenderPipelinesAccessor.toolvisuals$register(PIPELINE);}
    private static RenderType create(){
        var base=(RenderSetupAccessor)(Object)((RenderTypeAccessor)(Object)RenderTypes.debugQuads()).toolvisuals$state();
        var setup=RenderSetupAccessor.toolvisuals$create(PIPELINE,null,base.toolvisuals$textures(),false,false,
                base.toolvisuals$layering(),base.toolvisuals$transform(),base.toolvisuals$outline(),base.toolvisuals$outlineName(),false,false,true);
        return RenderTypeAccessor.toolvisuals$create("toolvisuals_accessories",setup);
    }
}
