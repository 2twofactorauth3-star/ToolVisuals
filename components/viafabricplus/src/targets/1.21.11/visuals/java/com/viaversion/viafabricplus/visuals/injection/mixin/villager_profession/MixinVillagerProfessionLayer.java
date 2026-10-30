package com.viaversion.viafabricplus.visuals.injection.mixin.villager_profession;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerProfessionLayer.class)
public abstract class MixinVillagerProfessionLayer {

    @Redirect(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/VillagerData;profession()Lnet/minecraft/core/Holder;"))
    private Holder<VillagerProfession> revertVillagerVisual(VillagerData instance) {
        if (VisualSettings.INSTANCE.hideVillagerProfession.getValue()) {
            return Minecraft.getInstance().getConnection().registryAccess().getOrThrow(VillagerProfession.NONE);
        } else {
            return instance.profession();
        }
    }

}
