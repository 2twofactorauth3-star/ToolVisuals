package ru.he3hauka.toolvisuals.client.gui.module;

import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public class VisualsButton extends Button {
    public enum Kind { TAB, CHIP, GHOST, ICON, SWITCH, OPTION, MODULE, MENU }
    final Kind kind;
    final UiPaint.Icon icon;
    public final Component detail;
    boolean moduleSound, muteClick, stackedTab;
    private final BooleanSupplier selected;
    private final Motion hover = new Motion(0);
    private final Motion selection;
    private UiPalette palette = new UiPalette(0);
    private float opacity = 1, elapsed, hoveredFor;

    public VisualsButton(int x, int y, int width, int height, Component label, Kind kind, UiPaint.Icon icon,
                  Component detail, Runnable action, BooleanSupplier selected) {
        super(x, y, width, height, label, button -> action.run(), DEFAULT_NARRATION);
        this.kind = kind;
        moduleSound = kind == Kind.MODULE;
        this.icon = icon;
        this.detail = detail;
        this.selected = selected;
        selection = new Motion(selected != null && selected.getAsBoolean() ? 1 : 0);
    }

    public void frame(UiPalette palette, float opacity, float elapsed) {
        this.palette = palette;
        this.opacity = opacity;
        this.elapsed = elapsed;
    }
    @Override public void playDownSound(net.minecraft.client.sounds.SoundManager manager) {
        var config=ru.he3hauka.toolvisuals.client.ToolVisualsClient.CONFIG;
        if(muteClick||moduleSound&&config.active(ru.he3hauka.toolvisuals.client.module.VisualModule.SOUND_CUSTOMIZER)
                &&config.flag(ru.he3hauka.toolvisuals.client.module.VisualSetting.SOUND_MODULES))return;
        super.playDownSound(manager);
    }

    public boolean showsHint() {
        return visible && isHovered() && hoveredFor > 0.65f && !detail.getString().isEmpty();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        isHovered = UiPaint.hovered(graphics, this, mouseX, mouseY);
        boolean chosen = selected != null && selected.getAsBoolean();
        UiPaint.Icon image = icon == UiPaint.Icon.STAR && chosen ? UiPaint.Icon.STAR_FILLED : icon;
        float h = hover.step(isHoveredOrFocused() ? 1 : 0, elapsed, 18);
        float on = selection.step(chosen ? 1 : 0, elapsed, 21);
        hoveredFor = isHovered() ? hoveredFor + elapsed : 0;
        float a = opacity * (active ? 1 : 0.4f);
        int x = getX(), y = getY(), w = getWidth(), height = getHeight();
        if (kind == Kind.SWITCH) {
            UiPaint.toggle(graphics, x + (w - 29) / 2f, y + (height - 16) / 2f, on, palette, a);
            return;
        }
        if(kind!=Kind.MODULE){
            int base=kind==Kind.OPTION?palette.field:kind==Kind.TAB?palette.inset:palette.button;
            int fill=UiPalette.mix(base,palette.hover,h*.7f);
            fill=UiPalette.mix(fill,palette.selected,on);
            float strength=kind==Kind.GHOST||kind==Kind.ICON?h:kind==Kind.TAB?Math.max(on,h*.4f):1;
            if(strength>.01f)UiPaint.rounded(graphics,x,y,w,height,7,UiPaint.alpha(fill,a*strength));
        }
        int foreground = kind==Kind.GHOST||kind==Kind.TAB||kind==Kind.ICON
                ?UiPalette.mix(palette.muted,palette.text,Math.max(on,h)):palette.text;
        if (kind == Kind.MODULE || kind == Kind.OPTION) foreground = palette.text;
        if (kind == Kind.ICON) {
            UiPaint.icon(graphics, image, x + (w - 16) / 2f, y + (height - 16) / 2f, 16,
                    icon == UiPaint.Icon.STAR && chosen ? palette.accentText : foreground, a);
            return;
        }
        if (kind == Kind.TAB && icon != null) {
            float scale = stackedTab ? .8f : .85f, size = stackedTab ? 11 : 13, gap = 5;
            String label = UiPaint.fit(graphics, getMessage(), w - (stackedTab ? 10 : size + gap + 12), scale);
            if (stackedTab) {
                UiPaint.icon(graphics, image, x + (w - size) / 2, y + 3, size, foreground, a);
                UiPaint.centered(graphics, label, x, y + 15, w, height - 16, scale, foreground, a);
            } else {
                float left = x + (w - size - gap - UiPaint.textWidth(graphics, label, scale)) / 2;
                UiPaint.icon(graphics, image, left, y + (height - size) / 2, size, foreground, a);
                UiPaint.baseline(graphics, label, left + size + gap,
                        UiPaint.textBaseline(graphics, y, height, scale), scale, foreground, a);
            }
            return;
        }
        float tx = x + 10;
        float labelScale = kind == Kind.CHIP || kind == Kind.GHOST ? 0.85f : 0.9f;
        if (icon == null && (kind == Kind.TAB || kind == Kind.CHIP || kind == Kind.GHOST)) {
            float scale = kind == Kind.TAB ? .8f : labelScale;
            UiPaint.centered(graphics, UiPaint.fit(graphics, getMessage(), w - 8, scale), x, y, w, height, scale, foreground, a);
            return;
        }
        int reserve = kind == Kind.OPTION || kind == Kind.MENU ? 46 : 10;
        if (kind == Kind.MODULE) {
            tx = x + 12;
            labelScale = 0.95f;
            reserve = 46;
        } else if (icon != null) {
            UiPaint.icon(graphics, image, x + 10, y + (height - 13) / 2f, 13, foreground, a);
            tx += 20;
        }
        if (kind == Kind.OPTION) tx = x + 8;
        String label = UiPaint.fit(graphics, getMessage(), x + w - tx - reserve, labelScale);
        float baseline = UiPaint.textBaseline(graphics, y, kind == Kind.MODULE ? 35 : height, labelScale);
        UiPaint.baseline(graphics, label, tx, baseline, labelScale, foreground, a);
        if (kind == Kind.MODULE) {
            UiPaint.baseline(graphics, UiPaint.fit(graphics, detail, w - 24, .8f), tx,
                    UiPaint.textBaseline(graphics, y + 34, 24, .8f), .8f, palette.muted, a);
        }
        if ((kind == Kind.MODULE && selected != null) || kind == Kind.OPTION)
            UiPaint.toggle(graphics, x + w - 39, y + ((kind == Kind.MODULE ? 35 : height) - 16) / 2f, on, palette, a);
        if (kind == Kind.MENU && on > .01f)
            UiPaint.icon(graphics, UiPaint.Icon.CHECK, x + w - 25, y + (height - 14) / 2f, 14, palette.accentText, a * on);
    }

    @Override
    protected net.minecraft.network.chat.MutableComponent createNarrationMessage() {
        if (selected != null) return UiI18n.tr("toolvisuals.toggle_narration", getMessage(),
                UiI18n.tr(selected.getAsBoolean() ? "toolvisuals.ui.on" : "toolvisuals.ui.off"));
        return super.createNarrationMessage();
    }
}
