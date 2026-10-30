package ru.he3hauka.toolvisuals.mixin.items;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.items.render.ItemBoundsState;

@Mixin(ItemStackRenderState.class)
public abstract class ItemBoundsMixin implements ItemBoundsState {
    @Unique private AABB toolvisuals$bounds;
    @Override public AABB toolvisuals$bounds(){return toolvisuals$bounds;}
    @Override public void toolvisuals$bounds(AABB bounds){toolvisuals$bounds=bounds;}
    @Inject(method="clear",at=@At("HEAD")) private void toolvisuals$reset(CallbackInfo callback){toolvisuals$bounds=null;}
}
