package com.viaversion.viafabricplus.injection.mixin.features.networking.downloading_terrain;

import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import com.viaversion.viafabricplus.injection.access.networking.downloading_terrain.IDownloadingTerrainScreen;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public abstract class MixinDownloadingTerrainScreen extends Screen implements IDownloadingTerrainScreen {

    @Shadow
    @Final
    private long loadStartTime;

    @Unique
    private int viaFabricPlus$tickCounter;

    @Unique
    private boolean viaFabricPlus$ready;

    @Unique
    private boolean viaFabricPlus$closeOnNextTick = false;

    public MixinDownloadingTerrainScreen(Text title) {
        super(title);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void modifyCloseCondition(CallbackInfo ci) {
        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_20_2)) {
            ci.cancel();

            if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_18)) {
                if (this.viaFabricPlus$ready) {
                    this.close();
                }

                if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_12_1)) {
                    this.viaFabricPlus$tickCounter++;
                    if (this.viaFabricPlus$tickCounter % 20 == 0) {
                        this.client.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(0));
                    }
                }
            } else {
                if (System.currentTimeMillis() > this.loadStartTime + 30000L) {
                    this.close();
                } else {
                    if (this.viaFabricPlus$closeOnNextTick) {
                        if (this.client.player == null) return;

                        final BlockPos blockPos = this.client.player.getBlockPos();
                        final boolean isOutOfHeightLimit = this.client.world != null && this.client.world.isOutOfHeightLimit(blockPos.getY());
                        if (isOutOfHeightLimit || this.client.worldRenderer.isRenderingReady(blockPos) || this.client.player.isSpectator() || !this.client.player.isAlive()) {
                            this.close();
                        }
                    } else {
                        if (ProtocolTranslator.getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_19_1)) {
                            this.viaFabricPlus$closeOnNextTick = this.viaFabricPlus$ready || System.currentTimeMillis() > this.loadStartTime + 2000;
                        } else {
                            this.viaFabricPlus$closeOnNextTick = this.viaFabricPlus$ready;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void viaFabricPlus$setReady() {
        this.viaFabricPlus$ready = true;
    }

}
