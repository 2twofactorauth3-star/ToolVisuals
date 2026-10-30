package ru.he3hauka.toolvisuals.mixin.accessories;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.*;

import ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.*;

@Mixin(AvatarRenderState.class)
public abstract class AccessoryStateMixin implements AccessoryVisualState {
    @Unique private AccessoryFrame toolvisuals$accessories;
    public AccessoryFrame toolvisuals$accessories(){return toolvisuals$accessories;}
    public void toolvisuals$accessories(AccessoryFrame frame){toolvisuals$accessories=frame;}
}
