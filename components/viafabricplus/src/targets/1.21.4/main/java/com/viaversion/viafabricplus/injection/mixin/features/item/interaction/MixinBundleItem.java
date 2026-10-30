package com.viaversion.viafabricplus.injection.mixin.features.item.interaction;

import net.minecraft.item.BundleItem;
import net.minecraft.util.ActionResult;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BundleItem.class)
public abstract class MixinBundleItem {

    @Redirect(method = "use", at = @At(value = "FIELD", target = "Lnet/minecraft/util/ActionResult;SUCCESS:Lnet/minecraft/util/ActionResult$Success;"))
    private ActionResult.Success dontSwing() {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_21_2)) {
            return ActionResult.CONSUME;
        } else {
            return ActionResult.SUCCESS;
        }
    }

}
