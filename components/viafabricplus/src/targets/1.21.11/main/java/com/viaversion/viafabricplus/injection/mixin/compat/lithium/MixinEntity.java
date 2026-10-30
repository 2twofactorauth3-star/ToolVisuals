package com.viaversion.viafabricplus.injection.mixin.compat.lithium;

import net.minecraft.world.entity.Entity;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Entity.class, priority = 1001  )
public abstract class MixinEntity {

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Redirect(method = "lithium$CollideMovement", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D", ordinal = 0), remap = false, require = 0)
    private static double alwaysSortYXZ(double a) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_13_2)) {
            return Double.MAX_VALUE;
        } else {
            return Math.abs(a);
        }
    }

}
