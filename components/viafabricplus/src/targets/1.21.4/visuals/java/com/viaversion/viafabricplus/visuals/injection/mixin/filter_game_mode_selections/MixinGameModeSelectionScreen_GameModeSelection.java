package com.viaversion.viafabricplus.visuals.injection.mixin.filter_game_mode_selections;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("DataFlowIssue")
@Mixin(GameModeSelectionScreen.GameModeSelection.class)
public abstract class MixinGameModeSelectionScreen_GameModeSelection {

    @Shadow
    @Final
    public static GameModeSelectionScreen.GameModeSelection SURVIVAL;

    @Shadow
    @Final
    public static GameModeSelectionScreen.GameModeSelection CREATIVE;

    @Inject(method = "next", at = @At("HEAD"), cancellable = true)
    private void unwrapGameModes(CallbackInfoReturnable<GameModeSelectionScreen.GameModeSelection> cir) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_7_6)) {
            switch ((GameModeSelectionScreen.GameModeSelection) (Object) this) {
                case CREATIVE -> cir.setReturnValue(SURVIVAL);
                case SURVIVAL -> {
                    if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_2_4tor1_2_5)) {
                        cir.setReturnValue(CREATIVE);
                    } else {
                        cir.setReturnValue(GameModeSelectionScreen.GameModeSelection.ADVENTURE);
                    }
                }
                case ADVENTURE -> cir.setReturnValue(CREATIVE);
            }
        }
    }

    @Inject(method = "getCommand", at = @At("HEAD"), cancellable = true)
    private void oldCommand(CallbackInfoReturnable<String> cir) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_2_4tor1_2_5)) {
            cir.setReturnValue(
                    "gamemode " + MinecraftClient.getInstance().getSession().getUsername() + ' ' + switch (((Enum<?>) (Object) this).ordinal()) {
                        case 0, 3 -> 1;
                        case 1, 2 -> 0;
                        default -> throw new AssertionError();
                    }
            );
        }
    }

}
