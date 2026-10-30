package com.viaversion.viafabricplus.injection.mixin.features.entity.r1_8_boat;

import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import com.google.common.collect.ImmutableMap;
import com.viaversion.viafabricplus.features.entity.r1_8_boat.BoatModel1_8;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LayerDefinitions.class)
public abstract class MixinLayerDefinitions {

    @ModifyVariable(method = "createRoots", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private static ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> addBoatModel(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        return builder.put(BoatModel1_8.MODEL_LAYER, BoatModel1_8.getTexturedModelData());
    }

}
