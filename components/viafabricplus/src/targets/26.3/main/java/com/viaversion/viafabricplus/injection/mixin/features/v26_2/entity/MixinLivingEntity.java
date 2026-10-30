package com.viaversion.viafabricplus.injection.mixin.features.v26_2.entity;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "swing", at = @At("HEAD"))
    private void sendSwingPacket(InteractionHand hand, SwingAnimation animation, boolean sendToSwingingEntity, CallbackInfoReturnable<Boolean> cir) {

        if ((Object) this instanceof LocalPlayer && ViaFabricPlus.api().targetVersion().olderThanOrEqualTo(ProtocolVersion.v26_2)) {
            final PacketWrapper swing = PacketWrapper.create(ServerboundPackets26_1.SWING, ViaFabricPlus.api().userConnection());
            swing.write(Types.VAR_INT, hand.ordinal());
            swing.scheduleSendToServer(Protocol26_2To26_3.class);
        }
    }

}
