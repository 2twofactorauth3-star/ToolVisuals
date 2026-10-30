package ru.he3hauka.toolvisuals.mixin.capes;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.he3hauka.toolvisuals.client.cape.CapeClient;

@Mixin(WingsLayer.class)
public abstract class CapeElytraMixin {
    @ModifyReturnValue(method="getPlayerElytraTexture",at=@At("RETURN"))
    private static Identifier toolvisuals$preserveWings(Identifier original,HumanoidRenderState state){
        return original!=null&&CapeClient.custom(original)&&state instanceof AvatarRenderState avatar&&avatar.skin.elytra()==null?null:original;
    }
}
