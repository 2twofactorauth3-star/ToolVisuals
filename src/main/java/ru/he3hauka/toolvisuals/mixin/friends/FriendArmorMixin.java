package ru.he3hauka.toolvisuals.mixin.friends;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.friends.FriendRenderState;
import ru.he3hauka.toolvisuals.client.friends.FriendStore.ArmorStyle;

@Mixin(LivingEntityRenderer.class)
public abstract class FriendArmorMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
    private void toolvisuals$friendArmor(LivingEntity entity,LivingEntityRenderState state,float partial,CallbackInfo ci) {
        if(state instanceof FriendRenderState colored) {
            int armor=0,body=0;
            if(entity instanceof Player player && ru.he3hauka.toolvisuals.client.friends.Friends.renderAllowed(entity)) {
                String name=player.getGameProfile().name();
                armor=ToolVisualsClient.FRIENDS.armorTint(name);
                boolean equipped=!player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()||!player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
                        ||!player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()||!player.getItemBySlot(EquipmentSlot.FEET).isEmpty();
                body=ToolVisualsClient.FRIENDS.bodyTint(name,equipped);
            }
            colored.toolvisuals$friendTint(armor);colored.toolvisuals$friendBodyTint(body);
            colored.toolvisuals$friendArmorFill(armor!=0&&ToolVisualsClient.FRIENDS.settings().armorStyle==ArmorStyle.FILL);
        }
    }
}
