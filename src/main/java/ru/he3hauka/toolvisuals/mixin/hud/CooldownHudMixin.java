package ru.he3hauka.toolvisuals.mixin.hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.hud.cooldown.Cooldowns;

@Mixin(ItemCooldowns.class)
public abstract class CooldownHudMixin {
    @Inject(method="addCooldown(Lnet/minecraft/world/item/ItemStack;I)V",at=@At("HEAD"))
    private void toolvisuals$rememberCooldownItem(ItemStack stack,int duration,CallbackInfo ci){Cooldowns.remember((ItemCooldowns)(Object)this,stack);}
    @Inject(method="addCooldown(Lnet/minecraft/resources/Identifier;I)V",at=@At("TAIL"))
    private void toolvisuals$cooldownAdded(Identifier group,int duration,CallbackInfo ci){Cooldowns.started((ItemCooldowns)(Object)this,group,duration);}
    @Inject(method="removeCooldown",at=@At("TAIL"))
    private void toolvisuals$cooldownRemoved(Identifier group,CallbackInfo ci){Cooldowns.ended((ItemCooldowns)(Object)this,group);}
    @Inject(method="tick",at=@At("TAIL"))
    private void toolvisuals$cooldownTick(CallbackInfo ci){Cooldowns.tick((ItemCooldowns)(Object)this);}
}
