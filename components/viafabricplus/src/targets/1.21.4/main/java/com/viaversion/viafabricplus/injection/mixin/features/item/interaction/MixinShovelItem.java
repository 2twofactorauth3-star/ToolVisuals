package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.item.ShovelItem;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Map;

@Mixin(ShovelItem.class)
public abstract class MixinShovelItem {

    @Redirect(method = "useOnBlock", slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/item/ShovelItem;PATH_STATES:Ljava/util/Map;")), at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0, remap = false))
    private Object disablePathAction(Map<Object, Object> instance, Object grassBlock) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            return null;
        } else {
            return instance.get(grassBlock);
        }
    }

}
