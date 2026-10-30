package ru.he3hauka.toolvisuals.mixin.render;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.ExtractionVisibility;
import ru.he3hauka.toolvisuals.client.optimization.occlusion.OcclusionService;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.friends.FriendLabels;
import ru.he3hauka.toolvisuals.client.friends.FriendRenderState;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @WrapMethod(method="extractEntity")
    private EntityRenderState toolvisuals$earlyVisibility(Entity entity,float partialTick,Operation<EntityRenderState> original) {
        if(!(entity instanceof net.minecraft.world.entity.LivingEntity)||!OcclusionService.hidden(entity))return original.call(entity,partialTick);
        var previous=ExtractionVisibility.current();
        ExtractionVisibility.restore(entity);
        try{return original.call(entity,partialTick);}finally{ExtractionVisibility.restore(previous);}
    }
    @Inject(method = "extractEntity", at = @At("RETURN"))
    private void toolvisuals$hideDecorations(Entity entity, float partialTick,
                                             CallbackInfoReturnable<EntityRenderState> cir) {
        var state = cir.getReturnValue();
        ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.count(ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Counter.ENTITY_CANDIDATES,1);
        boolean occluded=ExtractionVisibility.hidden(entity);
        ((ru.he3hauka.toolvisuals.client.optimization.occlusion.OccludedState)state).toolvisuals$occluded(occluded);
        if(occluded)state.shadowPieces.clear();
        if (NoRender.hides(NAME_TAGS)) {
            state.nameTag = null;
            state.scoreText = null;
        }
        if (NoRender.hides(ENTITY_FIRE)) state.displayFireAnimation = false;
        if (NoRender.hides(ENTITY_SHADOWS)) state.shadowPieces.clear();
        if (NoRender.hides(LEASHES)) state.leashStates = null;
        if (NoRender.hides(GLOWING)) state.outlineColor = EntityRenderState.NO_OUTLINE;
        if(state instanceof FriendRenderState friend) {
            friend.toolvisuals$friendLabel(null);
            if(entity instanceof Player player && ru.he3hauka.toolvisuals.client.friends.Friends.renderAllowed(entity)) {
                String name=player.getGameProfile().name();
                state.nameTag=FriendLabels.name(ToolVisualsClient.FRIENDS,name,state.nameTag);
                friend.toolvisuals$friendLabel(FriendLabels.badge(ToolVisualsClient.FRIENDS,name,state.nameTag,ToolVisualsClient.CONFIG.language()));
            }
        }
    }
}

