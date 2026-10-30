package ru.he3hauka.toolvisuals.client.items;

import java.util.WeakHashMap;

import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.world.entity.item.ItemEntity;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.ITEM_PHYSIC;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ItemPhysic {
    public interface State {
        void toolvisuals$physics(boolean active,boolean ground,float pitch,float yaw);
        boolean toolvisuals$physical();boolean toolvisuals$ground();float toolvisuals$pitch();float toolvisuals$yaw();
    }
    private static final WeakHashMap<ItemEntity,Orientation> ROTATIONS=new WeakHashMap<>();
    private static final class Orientation { float pitch,time;Orientation(float pitch,float time){this.pitch=pitch;this.time=time;} }
    private ItemPhysic() {}
    public static void extract(ItemEntity entity,ItemEntityRenderState state,float partial) {
        var config=ToolVisualsClient.CONFIG;var output=(State)state;
        boolean ground=entity.onGround();
        boolean enabled=config.active(ITEM_PHYSIC)&&(!ground||config.flag(PHYSIC_GROUND));
        if(!enabled){output.toolvisuals$physics(false,ground,0,0);ROTATIONS.remove(entity);return;}
        float time=entity.getAge()+partial,seed=entity.bobOffs*(float)(180/Math.PI);
        float target=ground?90:config.flag(PHYSIC_SPIN)?time*9*config.number(PHYSIC_SPEED)+seed:0;
        var rotation=ROTATIONS.computeIfAbsent(entity,e->new Orientation(target,time));
        float delta=Math.clamp(time-rotation.time,0,2);rotation.time=time;
        rotation.pitch+=net.minecraft.util.Mth.wrapDegrees(target-rotation.pitch)*(1-(float)Math.exp(-delta*.45));
        float yaw=seed+(ground||!config.flag(PHYSIC_SPIN)?0:time*config.number(PHYSIC_SPEED)*2);
        output.toolvisuals$physics(true,ground,rotation.pitch,yaw);
    }
    public static float floorOffset(net.minecraft.world.phys.AABB box,float degrees) {
        double angle=Math.toRadians(degrees),c=Math.cos(angle),s=Math.sin(angle);
        double minimum=Math.min(box.minY*c,box.maxY*c)-Math.max(box.minZ*s,box.maxZ*s);
        return (float)-minimum+.025f;
    }
}
