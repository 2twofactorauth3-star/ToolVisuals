package ru.he3hauka.toolvisuals.mixin.optimization;

import java.util.List;
import java.util.RandomAccess;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.items.GroundItemBatch;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_FAST_VISUALS;

@Mixin(ItemFeatureRenderer.class)
public abstract class GroundItemBatchMixin extends RenderTypeFeatureRenderer<ItemFeatureRenderer.Submit> {
    @Shadow @Final private QuadInstance quadInstance;
    @Shadow private void prepareSubmit(ItemFeatureRenderer.Submit submit) { throw new AssertionError(); }

    @WrapMethod(method="buildGroup")
    private void toolvisuals$groupGroundItems(FeatureFrameContext context,List<ItemFeatureRenderer.Submit> submits,Operation<Void> original) {
        if(!Optimizations.enabled(OPT_FAST_VISUALS)||submits.size()<2||!(submits instanceof RandomAccess)) {
            original.call(context,submits);
            return;
        }
        int index=0;
        while(index<submits.size()) {
            var submit=submits.get(index);
            int end=GroundItemBatch.eligible(submit)?GroundItemBatch.end(submits,index):index+1;
            var type=end-index>=2?GroundItemBatch.renderType(submit.quads()):null;
            if(type==null){prepareSubmit(submit);index++;}
            else {
                GroundItemBatch.emit(submits,index,end,getVertexBuilder(type),quadInstance);
                Diagnostics.count(Diagnostics.Counter.ITEM_BATCHED,end-index);
                index=end;
            }
        }
    }
}
