package ru.he3hauka.toolvisuals.client.armor.render;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import ru.he3hauka.toolvisuals.client.friends.FriendStore.ArmorStyle;
import ru.he3hauka.toolvisuals.mixin.render.RenderPipelinesAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderSetupAccessor;
import ru.he3hauka.toolvisuals.mixin.render.RenderTypeAccessor;

public final class ArmorRenderTypes {
    private static final LinkedHashMap<RenderType,EnumMap<ArmorStyle,RenderType>> TYPES=new LinkedHashMap<>(16,.75f,true);
    private static final RenderPipeline OVERLAY=pipeline(false),FILL=pipeline(true);
    private ArmorRenderTypes() {}
    public static void initialize(){RenderPipelinesAccessor.toolvisuals$register(OVERLAY);RenderPipelinesAccessor.toolvisuals$register(FILL);}
    private static RenderPipeline pipeline(boolean fill) {
        var builder=RenderPipeline.builder(RenderPipelinesAccessor.toolvisuals$entitySnippet()).withLocation(Identifier.parse("toolvisuals:pipeline/armor_tint"+(fill?"_fill":"")))
                .withVertexShader(Identifier.parse("toolvisuals:core/armor_tint")).withFragmentShader(Identifier.parse("toolvisuals:core/armor_tint"))
                .withShaderDefine("ALPHA_CUTOUT",.1f).withShaderDefine("NO_OVERLAY").withShaderDefine("PER_FACE_LIGHTING").withCull(false).withoutBlend();
        if(fill)builder.withShaderDefine("TOOLVISUALS_SOLID_FILL");return builder.build();
    }
    public static RenderType renderType(RenderType original,boolean fill) {
        if(original.pipeline()!=RenderPipelines.ARMOR_CUTOUT_NO_CULL)return original;
        var style=fill?ArmorStyle.FILL:ArmorStyle.OVERLAY;var variants=TYPES.get(original);
        if(variants!=null&&variants.containsKey(style))return variants.get(style);
        var state=(RenderSetupAccessor)(Object)((RenderTypeAccessor)(Object)original).toolvisuals$state();
        var setup=RenderSetupAccessor.toolvisuals$create(fill?FILL:OVERLAY,state.toolvisuals$textures(),state.toolvisuals$lightmap(),state.toolvisuals$overlay(),
                state.toolvisuals$layering(),state.toolvisuals$output(),state.toolvisuals$transform(),state.toolvisuals$outline(),state.toolvisuals$crumbling(),state.toolvisuals$sort(),state.toolvisuals$bufferSize());
        var result=RenderTypeAccessor.toolvisuals$create(fill?"toolvisuals_friend_armor_fill":"toolvisuals_armor_color",setup);
        if(variants==null){variants=new EnumMap<>(ArmorStyle.class);TYPES.put(original,variants);}variants.put(style,result);
        if(TYPES.size()>128)TYPES.remove(TYPES.keySet().iterator().next());return result;
    }
}
