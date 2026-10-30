package ru.he3hauka.toolvisuals.client.armor.render;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import ru.he3hauka.toolvisuals.client.friends.FriendStore.ArmorStyle;
import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderSetupAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderTypeAccessor;

public final class ArmorRenderTypes {
    private static final LinkedHashMap<RenderType, EnumMap<ArmorStyle,RenderType>> TYPES = new LinkedHashMap<>(16, .75f, true);
    private static final RenderPipeline NORMAL = pipeline(false,false), GLINT = pipeline(true,false),
            FILL = pipeline(false,true), FILL_GLINT = pipeline(true,true);
    private ArmorRenderTypes() {}
    public static void initialize() {
        for(var pipeline:new RenderPipeline[]{NORMAL,GLINT,FILL,FILL_GLINT})RenderPipelinesAccessor.toolvisuals$register(pipeline);
    }
    private static RenderPipeline pipeline(boolean glint,boolean fill) {
        var snippets = glint ? new RenderPipeline.Snippet[]{RenderPipelinesAccessor.toolvisuals$entitySnippet(), RenderPipelines.GLINT_SNIPPET}
                : new RenderPipeline.Snippet[]{RenderPipelinesAccessor.toolvisuals$entitySnippet()};
        var builder=RenderPipeline.builder(snippets).withLocation(Identifier.parse("toolvisuals:pipeline/armor_tint"+(fill?"_fill":"")+(glint?"_glint":"")))
                .withVertexShader(Identifier.parse("toolvisuals:core/armor_tint")).withFragmentShader(Identifier.parse("toolvisuals:core/armor_tint"))
                .withShaderDefine("ALPHA_CUTOUT", .1f).withShaderDefine("NO_OVERLAY").withShaderDefine("PER_FACE_LIGHTING")
                .withCull(false).withColorTargetState(ColorTargetState.DEFAULT);
        if(fill)builder.withShaderDefine("TOOLVISUALS_SOLID_FILL");
        return builder.build();
    }
    public static RenderType renderType(RenderType original,boolean fill) {
        var style=fill?ArmorStyle.FILL:ArmorStyle.OVERLAY;
        var variants=TYPES.get(original);
        var result=variants==null?null:variants.get(style);
        if (result != null) return result;
        if (original.pipeline() != RenderPipelines.ARMOR_CUTOUT_NO_CULL && original.pipeline() != RenderPipelines.ARMOR_CUTOUT_NO_CULL_GLINT) return original;
        var setup = ((RenderTypeAccessor)(Object)original).toolvisuals$state();
        var access = (RenderSetupAccessor)(Object)setup;
        boolean glint=original.pipeline()==RenderPipelines.ARMOR_CUTOUT_NO_CULL_GLINT;
        var pipeline=fill?(glint?FILL_GLINT:FILL):(glint?GLINT:NORMAL);
        var colored = RenderSetupAccessor.toolvisuals$create(pipeline, access.toolvisuals$oit(), access.toolvisuals$textures(),
                access.toolvisuals$lightmap(), access.toolvisuals$overlay(), access.toolvisuals$layering(), access.toolvisuals$transform(),
                access.toolvisuals$outline(), access.toolvisuals$outlineName(), access.toolvisuals$crumbling(), access.toolvisuals$sort(), access.toolvisuals$solid());
        result = RenderTypeAccessor.toolvisuals$create(fill?"toolvisuals_friend_armor_fill":"toolvisuals_armor_color",colored);
        if(variants==null){variants=new EnumMap<>(ArmorStyle.class);TYPES.put(original,variants);}
        variants.put(style,result);
        if (TYPES.size() > 128) TYPES.remove(TYPES.keySet().iterator().next());
        return result;
    }
}
