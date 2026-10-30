package com.viaversion.viafabricplus.injection.mixin.features.entity.allow_duplicated_uuid;

import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(EntityIndex.class)
public abstract class MixinEntityIndex<T extends EntityLike> {

    @Shadow
    @Final
    private Int2ObjectMap<T> idToEntity;

    @Redirect(method = "add", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z", remap = false))
    private boolean allowDuplicateUuid(Map<UUID, T> instance, Object o) {
        return instance.containsKey(o) && ProtocolTranslator.getTargetVersion().newerThan(ProtocolVersion.v1_16_4);
    }

    @Inject(method = "size", at = @At("HEAD"), cancellable = true)
    private void returnRealSize(CallbackInfoReturnable<Integer> cir) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_16_4)) {
            cir.setReturnValue(this.idToEntity.size());
        }
    }

}
