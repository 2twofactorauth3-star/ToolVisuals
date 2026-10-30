package ru.he3hauka.toolvisuals.mixin.hud;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.hud.tab.ExtraTab;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

@Mixin(PlayerTabOverlay.class)
public abstract class ExtraTabMixin {
    @Shadow private Component header;
    @Shadow private Component footer;
    @Shadow private List<PlayerInfo> getPlayerInfos() { throw new AssertionError(); }
    @ModifyExpressionValue(method={"extractRenderState","extractTablistScore","extractTablistHearts"},
            at=@At(value="FIELD",target="Lnet/minecraft/client/Minecraft;font:Lnet/minecraft/client/gui/Font;"))
    private Font toolvisuals$smoothTabFont(Font original) { return ExtraTab.font(original); }
    @ModifyArg(method = "getPlayerInfos", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;limit(J)Ljava/util/stream/Stream;"), index = 0)
    private long toolvisuals$unlimited(long original) { return ExtraTab.enabled() ? Long.MAX_VALUE : original; }
    @WrapMethod(method = "getPlayerInfos")
    private List<PlayerInfo> toolvisuals$allPlayers(Operation<List<PlayerInfo>> original) { return ExtraTab.players(original::call); }
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;header:Lnet/minecraft/network/chat/Component;"))
    private Component toolvisuals$header(Component original) { return ExtraTab.enabled() && !ToolVisualsClient.CONFIG.flag(TAB_HEADER) ? null : original; }
    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;footer:Lnet/minecraft/network/chat/Component;"))
    private Component toolvisuals$footer(Component original) { return ExtraTab.enabled() && !ToolVisualsClient.CONFIG.flag(TAB_FOOTER) ? null : original; }
    @WrapMethod(method = "extractRenderState")
    private void toolvisuals$viewport(GuiGraphicsExtractor graphics, int width, Scoreboard scoreboard, Objective objective, Operation<Void> original) {
        if (!ExtraTab.enabled()) { original.call(graphics, width, scoreboard, objective); return; }
        var layout = ExtraTab.begin((PlayerTabOverlay)(Object)this, width, graphics.guiHeight(), header, footer,
                scoreboard, objective, getPlayerInfos());
        try {
            graphics.pose().pushMatrix(); graphics.pose().scale(layout.scale, layout.scale);
            try { original.call(graphics, layout.width, scoreboard, objective); } finally { graphics.pose().popMatrix(); }
            ExtraTab.decorations(graphics);
        } finally { ExtraTab.end(); }
    }
}
