package ru.he3hauka.toolvisuals.mixin.items;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.*;

import ru.he3hauka.toolvisuals.client.items.ItemPhysic;

@Mixin(ItemEntityRenderState.class)
public class ItemPhysicStateMixin implements ItemPhysic.State {
    @Unique private boolean toolvisuals$physical,toolvisuals$ground;
    @Unique private float toolvisuals$pitch,toolvisuals$yaw;
    public void toolvisuals$physics(boolean active,boolean ground,float pitch,float yaw){toolvisuals$physical=active;toolvisuals$ground=ground;toolvisuals$pitch=pitch;toolvisuals$yaw=yaw;}
    public boolean toolvisuals$physical(){return toolvisuals$physical;}
    public boolean toolvisuals$ground(){return toolvisuals$ground;}
    public float toolvisuals$pitch(){return toolvisuals$pitch;}
    public float toolvisuals$yaw(){return toolvisuals$yaw;}
}
