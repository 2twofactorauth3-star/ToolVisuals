package com.viaversion.viafabricplus.injection.mixin.features.networking.player_abilities;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Abilities;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundPlayerAbilitiesPacket.class)
public abstract class MixinServerboundPlayerAbilitiesPacket {

    @Unique
    private Abilities viaFabricPlus$abilities;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/player/Abilities;)V", at = @At("RETURN"))
    private void capturePlayerAbilities(Abilities abilities, CallbackInfo ci) {
        this.viaFabricPlus$abilities = abilities;
    }

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeByte(I)Lnet/minecraft/network/FriendlyByteBuf;"))
    private FriendlyByteBuf implementFlags(FriendlyByteBuf instance, int value) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_15_2)) {
            if (viaFabricPlus$abilities.invulnerable) value |= 1;
            if (viaFabricPlus$abilities.mayfly) value |= 4;
            if (viaFabricPlus$abilities.instabuild) value |= 8;
        }

        return instance.writeByte(value);
    }

}
