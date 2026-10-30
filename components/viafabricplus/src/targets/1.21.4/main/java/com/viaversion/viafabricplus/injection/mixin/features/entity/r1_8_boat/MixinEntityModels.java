package com.viaversion.viafabricplus.injection.mixin.features.entity.r1_8_boat;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModels;

import com.google.common.collect.ImmutableMap;
import com.viaversion.viafabricplus.features.entity.r1_8_boat.BoatModel1_8;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityModels.class)
public abstract class MixinEntityModels {

    @ModifyVariable(method = "getModels", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private static ImmutableMap.Builder<EntityModelLayer, TexturedModelData> addBoatModel(ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder) {
        return builder.put(BoatModel1_8.MODEL_LAYER, BoatModel1_8.getTexturedModelData());
    }

}
