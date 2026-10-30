package ru.he3hauka.toolvisuals.client.armor.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.armor.ArmorColors;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.config.color.HexColor;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.color.HexColorField;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.armor.ArmorHud;
import ru.he3hauka.toolvisuals.client.hud.items.SmoothHudItems;
import ru.he3hauka.toolvisuals.client.hud.layout.HudEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.*;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ArmorSettingsScreen extends Screen {
    private final Screen parent;
    private final boolean status;
    private final ClientConfig config = ToolVisualsClient.CONFIG;
    private final List<VisualsButton> buttons = new ArrayList<>();
    private final List<EditBox> fields = new ArrayList<>();
    private float scale;
    private int px, py;
    private UiPalette palette;
    private int previewIndex;
    private long previewRevision = -1;
    private net.minecraft.client.renderer.entity.state.AvatarRenderState armorPreview;
    public ArmorSettingsScreen(Screen parent, boolean status) {
        super(Component.literal(status ? "ArmorColor" : "ArmorHUD")); this.parent = parent; this.status = status;
    }
    private static Component tr(String key) { return UiI18n.tr("toolvisuals.armor." + key); }
    @Override protected void init() {
        buttons.clear(); fields.clear();
        var viewport = new UiViewport(width, height, minecraft.getWindow().getGuiScale());
        scale = Math.min(viewport.scale, Math.min(width / 672f, height / 452f));
        px = Math.round((width / scale - 648) / 2); py = Math.round((height / scale - 428) / 2);
        button(596, 12, 38, 28, Component.empty(), this::onClose, null);
        var module = status ? ARMOR_COLOR : ARMOR_HUD;
        button(466, 12, 120, 28, tr("enabled"), () -> config.setActive(module, !config.active(module)), () -> config.active(module));
        if (!status) button(20, 380, 296, 30, tr("move"), () -> minecraft.gui.setScreen(new HudEditorScreen(this)), null);
        button(status ? 20 : 332, 380, status ? 608 : 296, 30, UiI18n.tr("toolvisuals.reset"), () -> { config.reset(module); rebuildWidgets(); }, null);
        if (status) {
            for (int i = 0; i < 5; i++) {
                final int index = i;
                int y = 94 + i * 48;
                EditBox threshold = field(48, y, 64, Integer.toString((int)config.value(ArmorColors.LEVELS[i])));
                threshold.setMaxLength(3);
                threshold.setResponder(value -> {
                    try { int n = Integer.parseInt(value); if (n >= 0 && n <= 100) { previewIndex = index; config.setValue(ArmorColors.LEVELS[index], n); } }
                    catch (NumberFormatException ignored) { }
                });
                var color=addWidget(new HexColorField(px+156,py+y-2,118,34,ArmorColors.COLORS[i].label(),
                        HexColor.format(config.colorStyle(ArmorColors.COLORS[i]).colors.getFirst(),true),()->openColor(index)));
                fields.add(color);
                color.setResponder(value->{
                    var parsed=color.color();
                    if(parsed!=null){previewIndex=index;config.setValue(ArmorColors.COLORS[index],parsed);}
                });
                button(286,y,42,30,Component.empty(),()->openColor(index),null);
            }
            button(20, 340, 300, 28, tr("show_self"), () -> config.setValue(ARMOR_STATUS_SELF, config.flag(ARMOR_STATUS_SELF) ? 0 : 1), () -> config.flag(ARMOR_STATUS_SELF));
            button(336, 340, 140, 28, tr("less_tint"), () -> config.setValue(ARMOR_TINT_STRENGTH, config.value(ARMOR_TINT_STRENGTH) - .05), null);
            button(488, 340, 140, 28, tr("more_tint"), () -> config.setValue(ARMOR_TINT_STRENGTH, config.value(ARMOR_TINT_STRENGTH) + .05), null);
        } else {
            for (int i = 0; i < 4; i++) {
                final int slot = i;
                for (int group = 0; group < 4; group++) {
                    final int selected = group;
                    button(228 + group * 98, 92 + i * 48, 90, 32,
                            UiI18n.tr("toolvisuals.choice.group_" + (group + 1)),
                            () -> config.setValue(ArmorHud.GROUPS[slot], selected), () -> (int)config.value(ArmorHud.GROUPS[slot]) == selected);
                }
            }
            button(20, 294, 296, 30, tr("join"), () -> { for (var setting : ArmorHud.GROUPS) config.setValue(setting, 0); }, null);
            button(332, 294, 296, 30, tr("split"), () -> { for (int i = 0; i < 4; i++) config.setValue(ArmorHud.GROUPS[i], i); }, null);
            button(20, 340, 296, 28, tr("bars"), () -> config.setValue(ARMOR_DURABILITY, config.flag(ARMOR_DURABILITY) ? 0 : 1), () -> config.flag(ARMOR_DURABILITY));
            button(332, 340, 296, 28, tr("full_bars"), () -> config.setValue(ARMOR_FULL_BAR, config.flag(ARMOR_FULL_BAR) ? 0 : 1), () -> config.flag(ARMOR_FULL_BAR));
        }
    }
    private VisualsButton button(int x, int y, int w, int h, Component label, Runnable action, java.util.function.BooleanSupplier selected) {
        boolean icon = label.getString().isEmpty();
        var b = addWidget(new VisualsButton(px + x, py + y, w, h, label,
                icon ? VisualsButton.Kind.ICON : VisualsButton.Kind.CHIP,
                icon ? (x == 596 ? UiPaint.Icon.BACK : UiPaint.Icon.RENDER) : null,
                icon ? UiI18n.tr(x == 596 ? "toolvisuals.ui.back" : "toolvisuals.armor.next_color") : Component.empty(), action, selected));
        buttons.add(b); return b;
    }
    private void openColor(int index){
        minecraft.gui.setScreen(new ColorEditorScreen(this,ArmorColors.COLORS[index].label(),config.colorStyle(ArmorColors.COLORS[index]),style->{
            previewIndex=index;config.setColorStyle(ArmorColors.COLORS[index],style);
        }));
    }
    private EditBox field(int x, int y, int w, String value) {
        var field = addWidget(new EditBox(UiFont.get(), px + x, py + y, w, 30, title) {
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
                g.pose().pushMatrix(); g.pose().translate(0, UiFont.centeredTextOffset(getHeight()));
                super.extractWidgetRenderState(g, mx, my, partial); g.pose().popMatrix();
            }
        });
        field.setBordered(false); field.setTextShadow(false); field.setValue(value); fields.add(field); return field;
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) { g.fill(0, 0, width, height, 0xa00a0b14); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        palette = new UiPalette(config.lightTheme() ? 1 : 0);
        int mx = Math.round(mouseX / scale), my = Math.round(mouseY / scale);
        g.pose().pushMatrix(); g.pose().scale(scale, scale);
        UiPaint.rounded(g, px, py, 648, 428, 14, palette.panel);
        UiPaint.baseline(g, title.getString(), px + 20, UiPaint.textBaseline(g, py + 10, 32, 1.2f), 1.2f, palette.text, 1);
        UiPaint.text(g, tr(status ? "threshold_hint" : "group_hint"), px + 20, py + 54, .75f, palette.muted, 1);
        if (status) {
            for (int i = 0; i < 5; i++) {
                int y = py + 94 + i * 48;
                int color = 0xff000000 | config.color(ArmorColors.COLORS[i]);
                UiPaint.rounded(g, px + 20, y, 18, 30, 5, color);
                UiPaint.text(g, "%", px + 120, y + 6, .8f, palette.muted, 1);
                UiPaint.rounded(g, px + 40, y - 2, 76, 34, 5, palette.raised);
                if (previewIndex == i) UiPaint.rounded(g, px + 18, y - 3, 312, 36, 5, UiPaint.alpha(color, .08f));
            }
            drawArmorPreview(g);
            UiPaint.centered(g, UiI18n.tr("toolvisuals.armor.preview_percent", (int)config.value(ArmorColors.LEVELS[previewIndex]),
                    Math.round(config.number(ARMOR_TINT_STRENGTH) * 100)).getString(), px + 336, py + 314, 292, 22, .75f, palette.muted, 1);
        } else {
            String[] names = {"helmet", "chestplate", "leggings", "boots"};
            for (int i = 0; i < 4; i++) {
                int y = py + 92 + i * 48;
                SmoothHudItems.item(g, ArmorHud.preview(i), px + 28, y + 8);
                UiPaint.baseline(g, tr(names[i]).getString(), px + 58, UiPaint.textBaseline(g, y, 32, .8f), .8f, palette.text, 1);
            }
        }
        for (var b : buttons) { b.frame(palette, 1, 1 / 60f); b.extractRenderState(g, mx, my, partial); }
        for(var field:fields){
            if(field instanceof HexColorField hex)hex.frame(palette);else field.setTextColor(palette.accentText);
            field.extractRenderState(g,mx,my,partial);
        }
        g.pose().popMatrix();
    }
    private MouseButtonEvent adjusted(MouseButtonEvent e) { return new MouseButtonEvent(e.x() / scale, e.y() / scale, e.buttonInfo()); }
    @Override public boolean mouseClicked(MouseButtonEvent e, boolean twice) {
        var point = adjusted(e);
        if (status && point.x() >= px + 18 && point.x() < px + 332 && point.y() >= py + 94 && point.y() < py + 334) {
            previewIndex = Math.clamp((int)(point.y() - py - 94) / 48, 0, 4);
        }
        return super.mouseClicked(point, twice);
    }
    private void drawArmorPreview(GuiGraphicsExtractor g) {
        if (minecraft.player == null) return;
        if (armorPreview == null || previewRevision != config.revision() || (int)armorPreview.ageInTicks != previewIndex) {
            armorPreview = (net.minecraft.client.renderer.entity.state.AvatarRenderState)minecraft.getEntityRenderDispatcher()
                    .getRenderer(minecraft.player).createRenderState(minecraft.player, 1);
            ru.he3hauka.toolvisuals.client.animation.EmoteVisualState.clearPreview(armorPreview);ru.he3hauka.toolvisuals.client.cosmetic.accessory.render.AccessoryVisualState.clear(armorPreview);
            armorPreview.ageInTicks = previewIndex;
            armorPreview.nameTag = null; armorPreview.scoreText = null; armorPreview.shadowPieces.clear();
            armorPreview.leashStates = null; armorPreview.displayFireAnimation = false; armorPreview.isInvisible = false;
            armorPreview.outlineColor = 0; armorPreview.bodyRot = 180; armorPreview.yRot = 0; armorPreview.xRot = 0;
            armorPreview.walkAnimationSpeed = 0; armorPreview.walkAnimationPos = 0; armorPreview.hasRedOverlay = false;
            armorPreview.pose = net.minecraft.world.entity.Pose.STANDING;
            var items = new net.minecraft.world.item.ItemStack[4];
            double percent = config.value(ArmorColors.LEVELS[previewIndex]);
            for (int i = 0; i < 4; i++) {
                items[i] = ArmorHud.preview(i).copy();
                items[i].setDamageValue((int)Math.ceil(items[i].getMaxDamage() * (1 - percent / 100)));
            }
            armorPreview.headEquipment = items[0]; armorPreview.chestEquipment = items[1];
            armorPreview.legsEquipment = items[2]; armorPreview.feetEquipment = items[3];
            ((ru.he3hauka.toolvisuals.client.armor.ArmorTint.Preview)armorPreview).toolvisuals$armorPreview(true);
            previewRevision = config.revision();
        }
        var first = g.pose().transformPosition(px + 356, py + 84, new org.joml.Vector2f());
        var last = g.pose().transformPosition(px + 624, py + 308, new org.joml.Vector2f());
        float units = (float)Math.hypot(g.pose().m00(), g.pose().m01());
        g.entity(armorPreview, 106 * units, new org.joml.Vector3f(0, .9f, 0),
                new org.joml.Quaternionf().rotationZ((float)Math.PI), new org.joml.Quaternionf(),
                Math.round(first.x), Math.round(first.y), Math.round(last.x), Math.round(last.y));
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) { return super.mouseReleased(adjusted(e)); }
    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { return super.mouseDragged(adjusted(e), dx / scale, dy / scale); }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / scale, y / scale); }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
