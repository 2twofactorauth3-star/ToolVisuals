package ru.he3hauka.toolvisuals.client.render.vignette;

import java.lang.ref.WeakReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.cosmetic.style.ColorPalette;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.DAMAGE_VIGNETTE;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class DamageVignette {
    private static final VignettePulse PULSE=new VignettePulse();
    private static WeakReference<Object> owner=new WeakReference<>(null);
    private static float health,absorption;
    private static long revision=-1,lastFrame;
    private static VignetteCurve curve;
    private static double opacity;
    private static int color;

    private DamageVignette() {}

    public static void tick(Minecraft client) {
        var player=client.player;
        if (owner.get()!=player) {
            owner=new WeakReference<>(player);PULSE.clear();opacity=0;lastFrame=0;
            health=player==null?0:player.getHealth();absorption=player==null?0:player.getAbsorptionAmount();
        }
        if (player==null) return;
        float nextHealth=player.getHealth(),nextAbsorption=player.getAbsorptionAmount();
        if (ToolVisualsClient.CONFIG.active(DAMAGE_VIGNETTE) && nextHealth>0
                && (nextHealth<health-.001f || nextAbsorption<absorption-.001f&&player.hurtTime>0)) PULSE.trigger(System.nanoTime());
        health=nextHealth;absorption=nextAbsorption;
        if (!ToolVisualsClient.CONFIG.active(DAMAGE_VIGNETTE) || !player.isAlive()) { PULSE.clear();opacity=0;lastFrame=0; }
    }

    public static void draw(GuiGraphicsExtractor graphics) {
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;
        if (!config.active(DAMAGE_VIGNETTE)||client.player==null||client.level==null||!client.player.isAlive()
                ||client.player.isSpectator()||client.gui.hud.isHidden()||client.gui.screen()!=null
                ||ru.he3hauka.toolvisuals.client.replay.playback.ReplayScope.active()) { opacity=0;lastFrame=0;return; }
        if (curve==null||revision!=config.revision()) { curve=VignetteSettings.curve(config);revision=config.revision(); }
        long now=System.nanoTime();
        double hp=client.player.getHealth()+(config.flag(VIGNETTE_ABSORPTION)?client.player.getAbsorptionAmount():0);
        var sample=curve.sample(hp,ColorPalette.clock());
        int mode=(int)config.value(VIGNETTE_MODE);
        double target=mode==1?0:sample.opacity();
        if (mode!=0) target=Math.max(target,PULSE.value(now,config.value(VIGNETTE_FLASH_DURATION))*Math.max(sample.opacity(),config.value(VIGNETTE_FLASH_STRENGTH)));
        double elapsed=lastFrame==0?1d/60:Math.clamp((now-lastFrame)/1_000_000_000d,0,.1);lastFrame=now;
        double time=target>opacity?Math.min(.05,config.value(VIGNETTE_TRANSITION)):config.value(VIGNETTE_TRANSITION);
        double blend=1-Math.exp(-elapsed/time);
        color=opacity<1d/255?sample.color():ColorPalette.mix(color,sample.color(),blend,ColorPalette.Space.RGB);
        opacity+=(target-opacity)*blend;
        VignetteOverlay.draw(graphics,0,0,graphics.guiWidth(),graphics.guiHeight(),color,opacity,
                config.value(VIGNETTE_WIDTH),config.value(VIGNETTE_SOFTNESS));
    }
}
