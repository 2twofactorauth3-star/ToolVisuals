package ru.he3hauka.toolvisuals.mixin.hud;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.*;

import ru.he3hauka.toolvisuals.client.hud.target.PortraitState;

@Mixin(EntityRenderState.class)
public abstract class PortraitStateMixin implements PortraitState {
    @Unique private float toolvisuals$portraitOpacity=1;
    @Override public float toolvisuals$portraitOpacity(){return toolvisuals$portraitOpacity;}
    @Override public void toolvisuals$portraitOpacity(float opacity){toolvisuals$portraitOpacity=opacity;}
}
