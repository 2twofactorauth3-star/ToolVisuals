package com.viaversion.viafabricplus.injection.mixin.features.item.filter_creative_tabs;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroup.DisplayContext;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroups.class)
public abstract class MixinItemGroups {

    @Shadow
    @Nullable
    private static ItemGroup.@Nullable DisplayContext displayContext;

    @Shadow
    private static void updateEntries(ItemGroup.DisplayContext displayContext) {
    }

    @Unique
    private static ProtocolVersion viaFabricPlus$version;

    @Unique
    private static int viaFabricPlus$state;

    @Inject(method = "updateDisplayContext", at = @At("HEAD"), cancellable = true)
    private static void trackLastVersion(FeatureSet enabledFeatures, boolean operatorEnabled, RegistryWrapper.WrapperLookup lookup, CallbackInfoReturnable<Boolean> cir) {
        if (viaFabricPlus$version != ProtocolTranslator.getTargetVersion() || viaFabricPlus$state != GeneralSettings.INSTANCE.removeNotAvailableItemsFromCreativeTab.getIndex()) {
            viaFabricPlus$version = ProtocolTranslator.getTargetVersion();
            viaFabricPlus$state = GeneralSettings.INSTANCE.removeNotAvailableItemsFromCreativeTab.getIndex();

            displayContext = new ItemGroup.DisplayContext(enabledFeatures, operatorEnabled, lookup);
            updateEntries(displayContext);

            cir.setReturnValue(true);
        }
    }

}
