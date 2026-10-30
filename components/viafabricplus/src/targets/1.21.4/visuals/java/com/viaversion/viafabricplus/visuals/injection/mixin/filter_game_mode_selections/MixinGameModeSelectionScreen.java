package com.viaversion.viafabricplus.visuals.injection.mixin.filter_game_mode_selections;

import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(GameModeSelectionScreen.class)
public abstract class MixinGameModeSelectionScreen extends Screen {

    @Mutable
    @Shadow
    @Final
    private static int UI_WIDTH;

    @Unique
    private GameModeSelectionScreen.GameModeSelection[] viaFabricPlusVisuals$unwrappedGameModes;

    public MixinGameModeSelectionScreen(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fixUIWidth(CallbackInfo ci) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_7_6)) {
            final List<GameModeSelectionScreen.GameModeSelection> selections = new ArrayList<>(Arrays.stream(GameModeSelectionScreen.GameModeSelection.values()).toList());

            selections.remove(GameModeSelectionScreen.GameModeSelection.SPECTATOR);
            if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_2_4tor1_2_5)) {
                selections.remove(GameModeSelectionScreen.GameModeSelection.ADVENTURE);
            }

            viaFabricPlusVisuals$unwrappedGameModes = selections.toArray(GameModeSelectionScreen.GameModeSelection[]::new);
            UI_WIDTH = viaFabricPlusVisuals$unwrappedGameModes.length * 31 - 5;
        }
    }

    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;VALUES:[Lnet/minecraft/client/gui/screen/GameModeSelectionScreen$GameModeSelection;"))
    private GameModeSelectionScreen.GameModeSelection[] removeNewerGameModes() {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_7_6)) {
            return viaFabricPlusVisuals$unwrappedGameModes;
        } else {
            return GameModeSelectionScreen.GameModeSelection.values();
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void disableInClassic(CallbackInfo ci) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.c0_28toc0_30)) {
            this.close();
        }
    }

}
