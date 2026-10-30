package ru.he3hauka.toolvisuals.client.items.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import ru.he3hauka.toolvisuals.mixin.items.ItemBoundsAccessor;
import ru.he3hauka.toolvisuals.mixin.items.ItemLayerAccessor;

public final class ItemGeometry {
    private ItemGeometry() {}
    public static AABB bounds(ItemStackRenderState item){
        var cache=(ItemBoundsState)item;var previous=cache.toolvisuals$bounds();if(previous!=null)return previous;
        var state=(ItemBoundsAccessor)(Object)item;double minX=Double.POSITIVE_INFINITY,minY=minX,minZ=minX,maxX=Double.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;var point=new Vector3f();var random=RandomSource.create(42);
        for(int layer=0;layer<state.toolvisuals$count();layer++){
            var data=(ItemLayerAccessor)(Object)state.toolvisuals$layers()[layer];var model=data.toolvisuals$model();if(model==null)continue;var pose=new PoseStack();data.toolvisuals$transform().apply(state.toolvisuals$leftHand(),pose);pose.translate(-.5,-.5,-.5);var matrix=pose.last().pose();
            for(int side=0;side<=6;side++){random.setSeed(42);for(var quad:model.getQuads(null,side==6?null:Direction.from3DDataValue(side),random)){var vertices=quad.getVertices();int stride=vertices.length/4;for(int vertex=0;vertex<4;vertex++){int offset=vertex*stride;point.set(Float.intBitsToFloat(vertices[offset]),Float.intBitsToFloat(vertices[offset+1]),Float.intBitsToFloat(vertices[offset+2]));matrix.transformPosition(point);minX=Math.min(minX,point.x);minY=Math.min(minY,point.y);minZ=Math.min(minZ,point.z);maxX=Math.max(maxX,point.x);maxY=Math.max(maxY,point.y);maxZ=Math.max(maxZ,point.z);}}}
        }
        var bounds=Double.isFinite(minX)?new AABB(minX,minY,minZ,maxX,maxY,maxZ):new AABB(-.5,-.5,-.5,.5,.5,.5);cache.toolvisuals$bounds(bounds);return bounds;
    }
}
