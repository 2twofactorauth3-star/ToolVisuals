package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.*;

import ru.he3hauka.toolvisuals.client.optimization.occlusion.OccludedState;

@Mixin(EntityRenderState.class)
public abstract class OccludedStateMixin implements OccludedState {
    @Unique private boolean toolvisuals$occluded;
    @Override public boolean toolvisuals$occluded(){return toolvisuals$occluded;}
    @Override public void toolvisuals$occluded(boolean value){toolvisuals$occluded=value;}
}
