package com.viaversion.viafabricplus.injection.mixin.base.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.settings.impl.GeneralSettings;
import com.viaversion.viafabricplus.util.ChatUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.vialegacy.protocol.classic.c0_28_30toa1_0_15.storage.ClassicProgressStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public abstract class MixinDownloadingTerrainScreen extends Screen {

    public MixinDownloadingTerrainScreen(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void renderClassicProgress(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (GeneralSettings.INSTANCE.showClassicLoadingProgressInConnectScreen.getValue()) {

            final UserConnection connection = ProtocolTranslator.getPlayNetworkUserConnection();
            if (connection == null) {
                return;
            }

            final ClassicProgressStorage classicProgressStorage = connection.get(ClassicProgressStorage.class);
            if (classicProgressStorage == null) {
                return;
            }

            context.drawCenteredTextWithShadow(
                    client.textRenderer,
                    ChatUtil.prefixText(classicProgressStorage.status),
                    width / 2,
                    height / 2 - 30,
                    -1
            );
        }
    }

}
