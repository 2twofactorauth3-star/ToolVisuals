package com.viaversion.viafabricplus.visuals.injection.mixin.filter_game_mode_selections;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameModeSwitcherScreen.class)
public abstract class MixinGameModeSwitcherScreen extends Screen {

    @Mutable
    @Shadow
    @Final
    private static int ALL_SLOTS_WIDTH;

    @Unique
    private GameModeSwitcherScreen.GameModeIcon[] viaFabricPlusVisuals$unwrappedGameModes;

    public MixinGameModeSwitcherScreen(Component title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fixUIWidth(CallbackInfo ci) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_7_6)) {
            final List<GameModeSwitcherScreen.GameModeIcon> selections = new ArrayList<>(Arrays.stream(GameModeSwitcherScreen.GameModeIcon.values()).toList());

            selections.remove(GameModeSwitcherScreen.GameModeIcon.SPECTATOR);
            if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.r1_2_4tor1_2_5)) {
                selections.remove(GameModeSwitcherScreen.GameModeIcon.ADVENTURE);
            }

            viaFabricPlusVisuals$unwrappedGameModes = selections.toArray(GameModeSwitcherScreen.GameModeIcon[]::new);
            ALL_SLOTS_WIDTH = viaFabricPlusVisuals$unwrappedGameModes.length * 31 - 5;
        }
    }

    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;VALUES:[Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;"))
    private GameModeSwitcherScreen.GameModeIcon[] removeNewerGameModes() {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_7_6)) {
            return viaFabricPlusVisuals$unwrappedGameModes;
        } else {
            return GameModeSwitcherScreen.GameModeIcon.values();
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void disableInClassic(CallbackInfo ci) {
        if (ViaFabricPlus.getImpl().getTargetVersion().olderThanOrEqualTo(LegacyProtocolVersion.c0_28toc0_30)) {
            this.onClose();
        }
    }

}
