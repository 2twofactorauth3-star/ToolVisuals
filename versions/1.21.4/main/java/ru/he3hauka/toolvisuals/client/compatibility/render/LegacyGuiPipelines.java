package ru.he3hauka.toolvisuals.client.compatibility.render;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class LegacyGuiPipelines {
    public static final Function<ResourceLocation,RenderType> GUI_TEXTURED=RenderType::guiTextured;
    private LegacyGuiPipelines() {}
}
