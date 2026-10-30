package ru.he3hauka.toolvisuals.mixin.hud;

import java.util.*;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.utility.bossbars.BossBars;

@Mixin(BossHealthOverlay.class)
public abstract class BossBarCustomizerMixin {
    @Shadow @Final private Map<UUID,LerpingBossEvent> events;
    @Inject(method="extractRenderState",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$customBosses(GuiGraphicsExtractor g,CallbackInfo ci) {
        if(!ToolVisualsClient.CONFIG.active(VisualModule.BOSS_BAR_CUSTOMIZER))return;
        BossBars.draw(g,events.values());ci.cancel();
    }
}
