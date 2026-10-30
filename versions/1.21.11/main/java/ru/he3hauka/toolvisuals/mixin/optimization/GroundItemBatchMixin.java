package ru.he3hauka.toolvisuals.mixin.optimization;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.items.GroundItemBatch;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_FAST_VISUALS;

@Mixin(ItemFeatureRenderer.class)
public abstract class GroundItemBatchMixin {
    @Unique private final PoseStack toolvisuals$pose=new PoseStack();
    @WrapMethod(method="render")
    private void toolvisuals$batch(SubmitNodeCollection collection,MultiBufferSource.BufferSource buffers,OutlineBufferSource outlines,Operation<Void> original) {
        var items=collection.getItemSubmits();
        if(!Optimizations.enabled(OPT_FAST_VISUALS)||items.size()<2){original.call(collection,buffers,outlines);return;}
        for(int index=0;index<items.size();) {
            var item=items.get(index);int end=GroundItemBatch.eligible(item)?GroundItemBatch.end(items,index):index+1;
            if(end-index>1) {
                var vertices=buffers.getBuffer(item.renderType());
                for(int next=index;next<end;next++){var entry=items.get(next);toolvisuals$pose.last().set(entry.pose());GroundItemQuadInvoker.toolvisuals$quads(toolvisuals$pose,vertices,entry.quads(),entry.tintLayers(),entry.lightCoords(),entry.overlayCoords());}
                Diagnostics.count(Diagnostics.Counter.ITEM_BATCHED,end-index);index=end;
            } else {
                toolvisuals$pose.last().set(item.pose());
                ItemRenderer.renderItem(item.displayContext(),toolvisuals$pose,buffers,item.lightCoords(),item.overlayCoords(),item.tintLayers(),item.quads(),item.renderType(),item.foilType());
                if(item.outlineColor()!=0){outlines.setColor(item.outlineColor());ItemRenderer.renderItem(item.displayContext(),toolvisuals$pose,outlines,item.lightCoords(),item.overlayCoords(),item.tintLayers(),item.quads(),item.renderType(),net.minecraft.client.renderer.item.ItemStackRenderState.FoilType.NONE);}
                index++;
            }
        }
    }
}
