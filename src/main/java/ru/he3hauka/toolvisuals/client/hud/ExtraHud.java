package ru.he3hauka.toolvisuals.client.hud;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPlacement;
import ru.he3hauka.toolvisuals.client.hud.target.TargetPortrait;
import ru.he3hauka.toolvisuals.client.hud.target.VisualTarget;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ExtraHud {
    private static final Motion targetOpacity = new Motion(0), health = new Motion(1);
    private static final Motion[] keys = new Motion[7];
    private static LivingEntity lastTarget;
    private static long lastFrame;
    private static String targetStats = "", targetName = "";
    private static LivingEntity statsTarget;
    private static long statsTick = Long.MIN_VALUE, statsRevision = -1;
    private static final class PotionRow {
        public final String title;
        public final boolean beneficial;
        public final Identifier icon;
        public final net.minecraft.world.effect.MobEffectInstance source;
        private String duration;
        private int second=Integer.MIN_VALUE;
        PotionRow(String title,String duration,boolean beneficial,Identifier icon){this.title=title;this.duration=duration;this.beneficial=beneficial;this.icon=icon;source=null;}
        PotionRow(net.minecraft.world.effect.MobEffectInstance effect){
            source=effect;String name=effect.getEffect().value().getDisplayName().getString();title=name+(effect.getAmplifier()>0?" "+(effect.getAmplifier()+1):"");beneficial=effect.getEffect().value().isBeneficial();icon=Hud.getMobEffectSprite(effect.getEffect());update();
        }
        void update(){if(source==null)return;int next=source.isInfiniteDuration()?-1:Math.max(0,source.getDuration()/20);if(second!=next){second=next;duration=HudFormats.duration(source.getDuration(),source.isInfiniteDuration());}}
    }
    private static final java.util.IdentityHashMap<net.minecraft.world.effect.MobEffectInstance,Integer> potionEffects=new java.util.IdentityHashMap<>();
    private static Object potionPlayer,potionLanguage;
    private static List<PotionRow> potionRows = List.of();
    private static final int[] MOVEMENT_KEYS = {InputConstants.KEY_W, InputConstants.KEY_A, InputConstants.KEY_S, InputConstants.KEY_D};
    private static final float[][] KEY_BOUNDS = {{33,0,30,26},{0,29,30,26},{33,29,30,26},{66,29,30,26},{0,58,96,22},{0,83,46,22},{50,83,46,22}};
    static { for (int i = 0; i < keys.length; i++) keys[i] = new Motion(0); }
    private ExtraHud() {}

    public static void reset() {
        lastTarget = null; lastFrame = 0; targetOpacity.snap(0); health.snap(1);
        statsTarget = null; potionRows = List.of();potionEffects.clear();potionPlayer=potionLanguage=null; statsTick = Long.MIN_VALUE;
        for (var key : keys) key.snap(0);
    }

     
    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) { potionRows = List.of();potionEffects.clear();potionPlayer=potionLanguage=null; return; }
        if (ToolVisualsClient.CONFIG.active(POTION_HUD)) updatePotions(client);
        else {potionRows = List.of();potionEffects.clear();potionPlayer=potionLanguage=null;}
    }

    private static void updatePotions(Minecraft client) {
        var effects=client.player.getActiveEffects();var language=net.minecraft.locale.Language.getInstance();
        boolean changed=potionPlayer!=client.player||potionLanguage!=language||effects.size()!=potionEffects.size()||potionRows.isEmpty()&&!effects.isEmpty();
        if(!changed)for(var effect:effects){var amplifier=potionEffects.get(effect);if(amplifier==null||amplifier!=effect.getAmplifier()){changed=true;break;}}
        if(changed){
            potionPlayer=client.player;potionLanguage=language;potionEffects.clear();
            var sorted=new java.util.ArrayList<net.minecraft.world.effect.MobEffectInstance>(effects);
            for(var effect:effects)potionEffects.put(effect,effect.getAmplifier());
            sorted.sort(Comparator.comparing(effect->effect.getEffect().value().getDisplayName().getString()));
            var rows=new java.util.ArrayList<PotionRow>(Math.min(8,sorted.size()));for(int i=0;i<Math.min(8,sorted.size());i++)rows.add(new PotionRow(sorted.get(i)));potionRows=List.copyOf(rows);
        }
        for(var row:potionRows)row.update();
    }

    public static void draw(GuiGraphicsExtractor graphics, UiPalette palette) {
        var client = Minecraft.getInstance();
        var config = ToolVisualsClient.CONFIG;
        long now = System.nanoTime();
        float dt = lastFrame == 0 ? 1 / 60f : Math.clamp((now - lastFrame) / 1_000_000_000f, 0, 1); lastFrame = now;
        if (HudPlacement.show(TARGET_HUD)) target(graphics, client, palette, dt);
        else { lastTarget = null; targetOpacity.snap(0); }
        if (HudPlacement.show(KEYSTROKES)) keystrokes(graphics, client, palette, dt);
        if (HudPlacement.show(POTION_HUD)) potions(graphics, client, palette);
    }

    private static float begin(GuiGraphicsExtractor graphics, VisualSetting scaleSetting) {
        var client = Minecraft.getInstance();
        float density = Math.clamp(client.getWindow().getHeight() / 720f, 1, 2);
        float scale = density * ToolVisualsClient.CONFIG.number(scaleSetting) / client.getWindow().getGuiScale();
        graphics.pose().pushMatrix(); graphics.pose().scale(scale, scale); return scale;
    }
    private static float x(GuiGraphicsExtractor g, float scale, float width, VisualSetting setting) {
        return HudFormats.position(g.guiWidth() / scale, width, HudPlacement.value(setting));
    }
    private static float y(GuiGraphicsExtractor g, float scale, float height, VisualSetting setting) {
        return HudFormats.position(g.guiHeight() / scale, height, HudPlacement.value(setting));
    }

    private static void target(GuiGraphicsExtractor g, Minecraft client, UiPalette p, float dt) {
        LivingEntity focused = HudPlacement.preview() ? client.player : VisualTarget.current();
        if (lastTarget != null && lastTarget.level() != client.level) { lastTarget = null; targetOpacity.snap(0); }
        if (focused != null && focused != lastTarget) {
            lastTarget = focused; health.snap(focused.getHealth() / Math.max(1, focused.getMaxHealth()));
        }
        float opacity = HudPlacement.preview() ? 1 : targetOpacity.step(focused == null ? 0 : 1, dt, 14);
        if (lastTarget == null || opacity < .01f) return;
        float scale = begin(g, TARGET_HUD_SCALE);
        long tick = client.level.getGameTime();
        if (!Optimizations.enabled(OPT_FAST_HUD) || statsTarget != lastTarget || tick - statsTick >= 2
                || statsRevision != ToolVisualsClient.CONFIG.revision()) {
            statsTarget = lastTarget; statsTick = tick; statsRevision = ToolVisualsClient.CONFIG.revision();
            targetName = lastTarget.getName().getString();
            targetStats = String.format(Locale.ROOT, "%.1f / %.1f  ·  %.1f m", lastTarget.getHealth(), lastTarget.getMaxHealth(), lastTarget.distanceTo(client.player));
        }
        float absorption = Math.clamp(lastTarget.getAbsorptionAmount() / Math.max(1, lastTarget.getMaxHealth()), 0, 1);
        float w = Math.clamp(57+Math.max(UiPaint.textWidth(g,targetName,.9f),UiPaint.textWidth(g,targetStats,.72f)),180,248);
        float h = absorption>0?58:54;
        float x = x(g, scale, w, TARGET_HUD_X), y = y(g, scale, h, TARGET_HUD_Y);
        HudPlacement.place(g, HudElement.TARGET, x, y, w, h);
        UiPaint.rounded(g, x, y, w, h, 7, UiPaint.alpha(p.hudPanel, opacity));
        UiPaint.rounded(g, x + 6, y + (h-36)/2, 36, 36, 7, UiPaint.alpha(p.hover, opacity));
        TargetPortrait.draw(g, lastTarget, x + 7, y + (h-34)/2, 34,opacity);
        UiPaint.baseline(g,UiPaint.fit(g,Component.literal(targetName),w-57,.9f),x+49,UiPaint.textBaseline(g,y+5,19,.9f),.9f,p.text,opacity);
        UiPaint.baseline(g,UiPaint.fit(g,Component.literal(targetStats),w-57,.72f),x+49,UiPaint.textBaseline(g,y+24,17,.72f),.72f,p.muted,opacity);
        float progress = health.step(Math.clamp(lastTarget.getHealth() / Math.max(1, lastTarget.getMaxHealth()), 0, 1), dt, 12);
        UiPaint.rounded(g, x + 49, y + 46, w - 57, 3, 1.5f, UiPaint.alpha(p.track, opacity));
        UiPaint.rounded(g, x + 49, y + 46, (w - 57) * progress, 3, 1.5f, UiPaint.alpha(p.accent, opacity));
        if (absorption > 0) UiPaint.rounded(g, x + 49, y + 52, (w - 57) * absorption, 2, 1, UiPaint.alpha(0xffffc776, opacity));
        g.pose().popMatrix(); g.pose().popMatrix();
    }

    private static void keystrokes(GuiGraphicsExtractor g, Minecraft client, UiPalette p, float dt) {
        var config = ToolVisualsClient.CONFIG;
        float scale = begin(g, KEYS_SCALE), w = 96, h = config.flag(KEYS_MOUSE) ? 105 : 80;
        float x = x(g, scale, w, KEYS_X), y = y(g, scale, h, KEYS_Y);
        HudPlacement.place(g, HudElement.KEYS, x, y, w, h);
        KeyMapping[] bindings = {client.options.keyUp, client.options.keyLeft, client.options.keyDown, client.options.keyRight,
                client.options.keyJump, client.options.keyAttack, client.options.keyUse};
        for (int i = 0; i < bindings.length; i++) {
            if (i > 4 && !config.flag(KEYS_MOUSE)) break;
            float press = keys[i].step(bindings[i].isDown() ? 1 : 0, dt, 22);
            var b = KEY_BOUNDS[i];
            UiPaint.rounded(g, x + b[0], y + b[1], b[2], b[3], 5, UiPalette.mix(p.hudPanel, p.accent, press * .75f));
            String label = bindings[i].getTranslatedKeyMessage().getString();
            if (i < 4 && bindings[i].matches(InputConstants.Type.KEYBOARD.getOrCreate(MOVEMENT_KEYS[i]))) label = "WASD".substring(i, i + 1);
            if (i == 4 && bindings[i].matches(InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_SPACE))) label = "SPACE";
            if (i == 5 && bindings[i].matches(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT))) label = "LMB";
            if (i == 6 && bindings[i].matches(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_RIGHT))) label = "RMB";
            UiPaint.centered(g, UiPaint.fit(g, Component.literal(label), b[2] - 10, .75f),
                    x + b[0], y + b[1], b[2], b[3], .75f, UiPalette.mix(p.muted, 0xffffffff, press), 1);
        }
        g.pose().popMatrix(); g.pose().popMatrix();
    }

    private static void potions(GuiGraphicsExtractor g, Minecraft client, UiPalette p) {
        if (!Optimizations.enabled(OPT_FAST_HUD)) updatePotions(client);
        var effects = potionRows;
        if (HudPlacement.samples()||effects.isEmpty() && HudPlacement.preview()) effects = List.of(
                new PotionRow(ru.he3hauka.toolvisuals.client.localization.UiI18n.tr("toolvisuals.hud.preview_speed").getString(), "2:35", true, Hud.getMobEffectSprite(MobEffects.SPEED)),
                new PotionRow(ru.he3hauka.toolvisuals.client.localization.UiI18n.tr("toolvisuals.hud.preview_strength").getString(), "1:20", true, Hud.getMobEffectSprite(MobEffects.STRENGTH)));
        if (effects.isEmpty()) return;
        int count = effects.size();
        float scale = begin(g, POTION_SCALE);
        boolean icons = ToolVisualsClient.CONFIG.flag(POTION_ICONS),time = ToolVisualsClient.CONFIG.flag(POTION_SECONDS);
        var heading=UiI18n.tr("toolvisuals.hud.effects");
        float w=UiPaint.textWidth(g,heading.getString(),.82f)+16;
        for(var effect:effects)w=Math.max(w,16+(icons?21:0)+UiPaint.textWidth(g,effect.title,.75f)
                +(time?8+UiPaint.textWidth(g,effect.duration,.72f):0));
        w=Math.clamp(w,100,240);
        int rowHeight=icons?22:20;
        float h=24+count*rowHeight+3;
        float x = x(g, scale, w, POTION_X), y = y(g, scale, h, POTION_Y);
        HudPlacement.place(g, HudElement.POTIONS, x, y, w, h);
        UiPaint.rounded(g, x, y, w, h, 7, p.hudPanel);
        UiPaint.baseline(g,heading.getString(),x+8,UiPaint.textBaseline(g,y,24,.82f),.82f,p.accentText,1);
        for (int i = 0; i < count; i++) {
            var effect = effects.get(i);
            float row=y+24+i*rowHeight,tx=x+8+(icons?21:0);
            float durationWidth=time?UiPaint.textWidth(g,effect.duration,.72f):0;
            if (icons) g.blitSprite(RenderPipelines.GUI_TEXTURED, effect.icon, Math.round(x + 8), Math.round(row+3), 16, 16);
            UiPaint.baseline(g,UiPaint.fit(g,Component.literal(effect.title),x+w-8-tx-(time?durationWidth+8:0),.75f),tx,
                    UiPaint.textBaseline(g,row,rowHeight,.75f),.75f,effect.beneficial ? p.text : 0xffff879c,1);
            if (time) {
                UiPaint.baseline(g,effect.duration,x+w-8-durationWidth,UiPaint.textBaseline(g,row,rowHeight,.72f),.72f,p.muted,1);
            }
        }
        g.pose().popMatrix(); g.pose().popMatrix();
    }
}
