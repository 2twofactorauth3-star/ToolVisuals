package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

import com.mojang.blaze3d.pipeline.*;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderSetupAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderTypeAccessor;

public final class AccessoryPipeline {
    private static final RenderPipeline PIPELINE=RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$debugFilledSnippet())
            .withLocation(Identifier.parse("toolvisuals:pipeline/accessories"))
            .withDepthTestFunction(com.mojang.blaze3d.platform.DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(true)
            .withoutBlend().withCull(false).build();
    public static final RenderType TYPE=create();
    private AccessoryPipeline(){}
    public static void initialize(){RenderPipelinesAccessor.toolvisuals$register(PIPELINE);}
    private static RenderType create(){
        var base=(RenderSetupAccessor)(Object)((RenderTypeAccessor)(Object)RenderTypes.debugQuads()).toolvisuals$state();
        var setup=RenderSetupAccessor.toolvisuals$create(PIPELINE,base.toolvisuals$textures(),false,false,
                base.toolvisuals$layering(),base.toolvisuals$output(),base.toolvisuals$transform(),base.toolvisuals$outline(),false,false,base.toolvisuals$bufferSize());
        return RenderTypeAccessor.toolvisuals$create("toolvisuals_accessories",setup);
    }
}
