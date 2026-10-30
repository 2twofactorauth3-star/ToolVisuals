package ru.he3hauka.toolvisuals.client.armor.render;

import java.util.LinkedHashMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.resources.ResourceLocation;
import ru.he3hauka.toolvisuals.client.compatibility.render.LegacyRenderType;

public final class ArmorRenderTypes {
    private static final ShaderProgram OVERLAY=shader(false),FILL=shader(true);
    private static final LinkedHashMap<RenderType,RenderType[]> TYPES=new LinkedHashMap<>(16,.75f,true);
    private ArmorRenderTypes() {}
    private static ShaderProgram shader(boolean fill){return new ShaderProgram(ResourceLocation.parse("toolvisuals:core/armor_tint"),DefaultVertexFormat.NEW_ENTITY,fill?ShaderDefines.builder().define("TOOLVISUALS_SOLID_FILL").build():ShaderDefines.EMPTY);}
    public static void initialize(){TYPES.clear();}
    public static RenderType renderType(RenderType original,boolean fill){var variants=TYPES.computeIfAbsent(original,type->new RenderType[2]);int index=fill?1:0;if(variants[index]==null)variants[index]=new LegacyRenderType(fill?"toolvisuals_armor_fill":"toolvisuals_armor_tint",original,()->RenderSystem.setShader(fill?FILL:OVERLAY));if(TYPES.size()>128)TYPES.remove(TYPES.keySet().iterator().next());return variants[index];}
}
