package ru.he3hauka.toolvisuals.client.optimization.items;

import java.util.List;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer.Submit;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

public final class GroundItemBatch {
    private GroundItemBatch() {}

    public static boolean eligible(Submit submit) {
        return submit.displayContext() == ItemDisplayContext.GROUND && submit.foilType() == FoilType.NONE
                && submit.outlineColor() == 0 && !submit.quads().isEmpty() && submit.quads().size() <= 64;
    }

    public static RenderType renderType(List<BakedQuad> quads) {
        var type = quads.getFirst().materialInfo().itemRenderType();
        if (type.hasBlending() || type.sortOnUpload() || !type.canConsolidateConsecutiveGeometry()) return null;
        for (int index = 1; index < quads.size(); index++)
            if (quads.get(index).materialInfo().itemRenderType() != type) return null;
        return type;
    }

    public static int end(List<Submit> submits, int start) {
        var quads = submits.get(start).quads();
        int end = start + 1;
        while (end < submits.size() && eligible(submits.get(end)) && submits.get(end).quads() == quads) end++;
        return end;
    }

    public static void emit(List<Submit> submits, int start, int end, VertexConsumer vertices, QuadInstance instance) {
        var quads = submits.get(start).quads();
        for (int index = start; index < end; index++) {
            var submit = submits.get(index);
            instance.setLightCoords(submit.lightCoords());
            instance.setOverlayCoords(submit.overlayCoords());
            for (int quadIndex = 0; quadIndex < quads.size(); quadIndex++) {
                var quad = quads.get(quadIndex);
                var material = quad.materialInfo();
                int tint = material.tintIndex();
                instance.setColor(material.isTinted() && tint >= 0 && tint < submit.tintLayers().length ? submit.tintLayers()[tint] : -1);
                vertices.putBakedQuad(submit.pose(), quad, instance);
            }
        }
    }
}
