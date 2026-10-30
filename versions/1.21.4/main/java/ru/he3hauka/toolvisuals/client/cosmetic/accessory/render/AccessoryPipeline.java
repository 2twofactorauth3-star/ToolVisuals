package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

import net.minecraft.client.renderer.RenderType;
import ru.he3hauka.toolvisuals.client.compatibility.render.WorldPipelines;

public final class AccessoryPipeline {
    public static final RenderType TYPE=WorldPipelines.OPAQUE;
    private AccessoryPipeline() {}
    public static void initialize(){WorldPipelines.initialize();}
}
