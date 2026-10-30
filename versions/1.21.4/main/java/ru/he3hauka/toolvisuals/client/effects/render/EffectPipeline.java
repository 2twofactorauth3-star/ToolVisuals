package ru.he3hauka.toolvisuals.client.effects.render;

import net.minecraft.client.renderer.RenderType;
import ru.he3hauka.toolvisuals.client.compatibility.render.WorldPipelines;

public final class EffectPipeline {
    public static final RenderType TYPE=WorldPipelines.TRANSLUCENT;
    private EffectPipeline() {}
    public static void initialize(){WorldPipelines.initialize();}
}
