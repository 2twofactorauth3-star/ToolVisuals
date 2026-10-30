package ru.he3hauka.toolvisuals.client.optimization.items;

import java.util.List;
import net.minecraft.client.renderer.SubmitNodeStorage.ItemSubmit;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.world.item.ItemDisplayContext;

public final class GroundItemBatch {
    private GroundItemBatch() {}
    public static boolean eligible(ItemSubmit item) {
        var type=item.renderType();
        return item.displayContext()==ItemDisplayContext.GROUND&&item.foilType()==FoilType.NONE&&item.outlineColor()==0
                &&!item.quads().isEmpty()&&item.quads().size()<=64&&type.pipeline().getBlendFunction().isEmpty()&&!type.sortOnUpload()&&type.canConsolidateConsecutiveGeometry();
    }
    public static int end(List<ItemSubmit> items,int start) {
        var first=items.get(start);int end=start+1;
        while(end<items.size()&&eligible(items.get(end))&&items.get(end).quads()==first.quads()&&items.get(end).renderType()==first.renderType())end++;
        return end;
    }
}
