package com.viaversion.viafabricplus.injection.mixin.features.networking.player_abilities;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UpdatePlayerAbilitiesC2SPacket.class)
public abstract class MixinUpdatePlayerAbilitiesC2SPacket {

    @Unique
    private PlayerAbilities viaFabricPlus$abilities;

    @Inject(method = "<init>(Lnet/minecraft/entity/player/PlayerAbilities;)V", at = @At("RETURN"))
    private void capturePlayerAbilities(PlayerAbilities abilities, CallbackInfo ci) {
        this.viaFabricPlus$abilities = abilities;
    }

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeByte(I)Lnet/minecraft/network/PacketByteBuf;"))
    private PacketByteBuf implementFlags(PacketByteBuf instance, int value) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2)) {
            if (viaFabricPlus$abilities.invulnerable) value |= 1;
            if (viaFabricPlus$abilities.allowFlying) value |= 4;
            if (viaFabricPlus$abilities.creativeMode) value |= 8;
        }

        return instance.writeByte(value);
    }

}
