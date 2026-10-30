package com.viaversion.viafabricplus.visuals.injection.mixin.r1_7_tab_list_style;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viaversion.viafabricplus.visuals.injection.access.r1_7_tab_list_tyle.IPlayerInfo;
import com.viaversion.viafabricplus.visuals.injection.access.r1_7_tab_list_tyle.IPlayerTabOverlay;
import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
public abstract class MixinPlayerTabOverlay implements IPlayerTabOverlay {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private static final Comparator<PlayerInfo> viaFabricPlusVisuals$FIFO_COMPARATOR = Comparator.comparingInt(e -> ((IPlayerInfo) e).viaFabricPlusVisuals$getIndex());

    @Unique
    private int viaFabricPlusVisuals$maxSlots;

    @Unique
    private boolean viaFabricPlusVisuals$hideSkins = true;

    @Inject(method = "getPlayerInfos", at = @At("HEAD"), cancellable = true)
    private void collectPlayerEntries(CallbackInfoReturnable<List<PlayerInfo>> result) {
        if (VisualSettings.INSTANCE.enableLegacyTablist.isEnabled()) {
            result.setReturnValue(this.minecraft.player.connection.getListedOnlinePlayers().stream()
                    .sorted(viaFabricPlusVisuals$FIFO_COMPARATOR)
                    .limit(viaFabricPlusVisuals$maxSlots)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), this::viaFabricPlusVisuals$transpose)));
        } else {
            viaFabricPlusVisuals$hideSkins = false;
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;isEncrypted()Z"))
    private boolean hideSkins(boolean original) {
        return original && !viaFabricPlusVisuals$hideSkins;
    }

    @Unique
    private List<PlayerInfo> viaFabricPlusVisuals$transpose(final List<PlayerInfo> list) {

        if (list.size() != viaFabricPlusVisuals$maxSlots) {
            viaFabricPlusVisuals$hideSkins = list.stream().noneMatch(e -> e.getProfile().properties().containsKey("textures"));
            return list;
        }

        final List<PlayerInfo> result = new ArrayList<>(list.size());

        final int columns = viaFabricPlusVisuals$maxSlots / PlayerTabOverlay.MAX_ROWS_PER_COL;
        boolean anyHasSkinData = false;
        for (int i = 0; i < viaFabricPlusVisuals$maxSlots; i++) {
            final int row = i % PlayerTabOverlay.MAX_ROWS_PER_COL;
            final int col = i / PlayerTabOverlay.MAX_ROWS_PER_COL;
            final PlayerInfo current = list.get(row * columns + col);
            result.add(current);
            anyHasSkinData = anyHasSkinData || current.getProfile().properties().containsKey("textures");
        }
        viaFabricPlusVisuals$hideSkins = !anyHasSkinData;
        return result;
    }

    @Override
    public void viaFabricPlusVisuals$setMaxPlayers(int maxPlayers) {
        this.viaFabricPlusVisuals$maxSlots = Math.min(200, Math.max(20, ((maxPlayers + PlayerTabOverlay.MAX_ROWS_PER_COL - 1) / PlayerTabOverlay.MAX_ROWS_PER_COL) * PlayerTabOverlay.MAX_ROWS_PER_COL));
    }

}
