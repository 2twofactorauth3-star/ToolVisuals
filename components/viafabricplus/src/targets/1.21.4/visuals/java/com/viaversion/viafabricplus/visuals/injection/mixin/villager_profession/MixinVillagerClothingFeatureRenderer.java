package com.viaversion.viafabricplus.visuals.injection.mixin.villager_profession;

import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;

import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerClothingFeatureRenderer.class)
public abstract class MixinVillagerClothingFeatureRenderer {

    @Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/VillagerData;getProfession()Lnet/minecraft/village/VillagerProfession;"))
    private VillagerProfession revertVillagerVisual(VillagerData instance) {
        if (VisualSettings.INSTANCE.hideVillagerProfession.getValue()) {
            return VillagerProfession.NONE;
        } else {
            return instance.getProfession();
        }
    }

}
