package ru.he3hauka.toolvisuals.mixin.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.render.filter.NoRender;

import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void toolvisuals$visualHud(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker delta, CallbackInfo ci) {
        long started=ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.start(ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.HUD);
        try{ru.he3hauka.toolvisuals.client.hud.VisualHud.draw(graphics);}
        finally{ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.end(ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.HUD,started);}
        ru.he3hauka.toolvisuals.client.optimization.diagnostics.DiagnosticHud.draw(graphics);
    }
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractRenderState(CallbackInfo ci) {
        ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement.beginFrame();
        if (NoRender.hides(ALL_HUD)) ci.cancel();
    }


    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractScoreboardSidebar(CallbackInfo ci) {
        if (NoRender.hides(SCOREBOARD)) ci.cancel();
    }

    @Inject(method = "extractChat", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractChat(CallbackInfo ci) {
        if (NoRender.hides(CHAT)) ci.cancel();
    }

    @Inject(method = "extractTabList", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractTabList(CallbackInfo ci) {
        if (NoRender.hides(PLAYER_LIST)) ci.cancel();
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractCrosshair(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker delta, CallbackInfo ci) {
        if (NoRender.hides(CROSSHAIR)) { ci.cancel(); return; }
        if (ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG.active(ru.he3hauka.toolvisuals.client.module.VisualModule.CROSSHAIR)) {
            ru.he3hauka.toolvisuals.client.hud.VisualHud.crosshair(graphics);
            ci.cancel();
        }
    }

    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractItemHotbar(CallbackInfo ci) {
        if (NoRender.hides(HOTBAR)) ci.cancel();
    }

    @Inject(method = "extractHearts", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractHearts(CallbackInfo ci) {
        if (NoRender.hides(HEALTH)) ci.cancel();
    }

    @Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true)
    private static void toolvisuals$extractArmor(CallbackInfo ci) {
        if (NoRender.hides(ARMOR)) ci.cancel();
    }

    @Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractFood(CallbackInfo ci) {
        if (NoRender.hides(FOOD)) ci.cancel();
    }

    @Inject(method = "extractAirBubbles", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractAirBubbles(CallbackInfo ci) {
        if (NoRender.hides(AIR)) ci.cancel();
    }

    @Inject(method = "extractVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractVehicleHealth(CallbackInfo ci) {
        if (NoRender.hides(VEHICLE_HEALTH)) ci.cancel();
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractEffects(CallbackInfo ci) {
        if (NoRender.hides(STATUS_EFFECTS) || ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG.active(
                ru.he3hauka.toolvisuals.client.module.VisualModule.POTION_HUD)) ci.cancel();
    }

    @Inject(method = "extractTitle", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractTitle(CallbackInfo ci) {
        if (NoRender.hides(TITLES)) ci.cancel();
    }

    @Inject(method = "extractOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractOverlayMessage(CallbackInfo ci) {
        if (NoRender.hides(ACTION_BAR)) ci.cancel();
    }


    @Inject(method = "extractSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractSelectedItemName(CallbackInfo ci) {
        if (NoRender.hides(ITEM_NAMES)) ci.cancel();
    }


    @Inject(method = "extractSleepOverlay", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractSleepOverlay(CallbackInfo ci) {
        if (NoRender.hides(SLEEP_OVERLAY)) ci.cancel();
    }

    @Inject(method = "extractDemoOverlay", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractDemoOverlay(CallbackInfo ci) {
        if (NoRender.hides(DEMO_OVERLAY)) ci.cancel();
    }

    @Inject(method = "extractSavingIndicator", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractSavingIndicator(CallbackInfo ci) {
        if (NoRender.hides(SAVING_INDICATOR)) ci.cancel();
    }

    @Inject(method = "extractSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractSpyglassOverlay(CallbackInfo ci) {
        if (NoRender.hides(SPYGLASS_OVERLAY)) ci.cancel();
    }

    @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractVignette(CallbackInfo ci) {
        if (NoRender.hides(VIGNETTE)) ci.cancel();
    }

    @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractPortalOverlay(CallbackInfo ci) {
        if (NoRender.hides(PORTAL_DISTORTION)) ci.cancel();
    }

    @Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$extractConfusionOverlay(CallbackInfo ci) {
        if (NoRender.hides(NAUSEA)) ci.cancel();
    }

    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void toolvisuals$hideEquipmentOverlay(GuiGraphicsExtractor graphics, Identifier texture, float alpha, CallbackInfo ci) {
        boolean freezing = texture.getPath().contains("powder_snow");
        if (NoRender.hides(freezing ? FREEZING_OVERLAY : EQUIPMENT_OVERLAY)) ci.cancel();
    }

    @Inject(method={"renderExperienceBar","renderExperienceLevel"},at=@At("HEAD"),cancellable=true)
    private void toolvisuals$experience(CallbackInfo callback){if(NoRender.hides(EXPERIENCE_BAR))callback.cancel();}
}
