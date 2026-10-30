package com.viaversion.viafabricplus.injection.mixin.features.item.sword_blocking;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter.BlockItemPacketRewriter1_21_4;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlockItemPacketRewriter1_21_4.class, remap = false)
public abstract class MixinBlockItemPacketRewriter1_21_4 {

    @Redirect(method = "appendItemDataFixComponents", at = @At(value = "INVOKE", target = "Lcom/viaversion/viaversion/api/protocol/version/ProtocolVersion;olderThanOrEqualTo(Lcom/viaversion/viaversion/api/protocol/version/ProtocolVersion;)Z"))
    private boolean changeSwordFixVersionRange(ProtocolVersion instance, ProtocolVersion other) {
        if (other == ProtocolVersion.v1_8) {
            return instance.betweenInclusive(LegacyProtocolVersion.b1_8tob1_8_1, ProtocolVersion.v1_8);
        } else {
            return instance.olderThanOrEqualTo(other);
        }
    }

}
