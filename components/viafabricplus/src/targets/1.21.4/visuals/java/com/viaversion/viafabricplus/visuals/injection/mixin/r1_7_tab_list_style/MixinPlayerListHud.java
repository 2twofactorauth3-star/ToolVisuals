package com.viaversion.viafabricplus.visuals.injection.mixin.r1_7_tab_list_style;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viaversion.viafabricplus.visuals.injection.access.r1_7_tab_list_tyle.IPlayerListEntry;
import com.viaversion.viafabricplus.visuals.injection.access.r1_7_tab_list_tyle.IPlayerListHud;
import com.viaversion.viafabricplus.visuals.settings.VisualSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(PlayerListHud.class)
public abstract class MixinPlayerListHud implements IPlayerListHud {

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private static final Comparator<PlayerListEntry> viaFabricPlusVisuals$FIFO_COMPARATOR = Comparator.comparingInt(e -> ((IPlayerListEntry) e).viaFabricPlusVisuals$getIndex());

    @Unique
    private int viaFabricPlusVisuals$maxSlots;

    @Unique
    private boolean viaFabricPlusVisuals$hideSkins = true;

    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    private void collectPlayerEntries(CallbackInfoReturnable<List<PlayerListEntry>> result) {
        if (VisualSettings.INSTANCE.enableLegacyTablist.isEnabled()) {
            result.setReturnValue(this.client.player.networkHandler.getListedPlayerListEntries().stream()
                    .sorted(viaFabricPlusVisuals$FIFO_COMPARATOR)
                    .limit(viaFabricPlusVisuals$maxSlots)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), this::viaFabricPlusVisuals$transpose)));
        } else {
            viaFabricPlusVisuals$hideSkins = false;
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;isEncrypted()Z"))
    private boolean hideSkins(boolean original) {
        return original && !viaFabricPlusVisuals$hideSkins;
    }

    @Unique
    private List<PlayerListEntry> viaFabricPlusVisuals$transpose(final List<PlayerListEntry> list) {

        if (list.size() != viaFabricPlusVisuals$maxSlots) {
            viaFabricPlusVisuals$hideSkins = list.stream().noneMatch(e -> e.getProfile().getProperties().containsKey("textures"));
            return list;
        }

        final List<PlayerListEntry> result = new ArrayList<>(list.size());

        final int columns = viaFabricPlusVisuals$maxSlots / PlayerListHud.MAX_ROWS;
        boolean anyHasSkinData = false;
        for (int i = 0; i < viaFabricPlusVisuals$maxSlots; i++) {
            final int row = i % PlayerListHud.MAX_ROWS;
            final int col = i / PlayerListHud.MAX_ROWS;
            final PlayerListEntry current = list.get(row * columns + col);
            result.add(current);
            anyHasSkinData = anyHasSkinData || current.getProfile().getProperties().containsKey("textures");
        }
        viaFabricPlusVisuals$hideSkins = !anyHasSkinData;
        return result;
    }

    @Override
    public void viaFabricPlusVisuals$setMaxPlayers(int maxPlayers) {
        this.viaFabricPlusVisuals$maxSlots = Math.min(200, Math.max(20, ((maxPlayers + PlayerListHud.MAX_ROWS - 1) / PlayerListHud.MAX_ROWS) * PlayerListHud.MAX_ROWS));
    }

}
