package ru.he3hauka.toolvisuals.client.compatibility.render;

import java.util.LinkedHashMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderType;

public final class PortraitRenderTypes {
    private static final LinkedHashMap<RenderType,RenderType> TYPES=new LinkedHashMap<>(32,.75f,true);
    private PortraitRenderTypes() {}
    public static RenderType transparent(RenderType original){var result=TYPES.computeIfAbsent(original,type->new LegacyRenderType("toolvisuals_portrait",type,()->{RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();}));if(TYPES.size()>128)TYPES.remove(TYPES.keySet().iterator().next());return result;}
}
