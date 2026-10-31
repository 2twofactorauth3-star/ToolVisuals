package ru.he3hauka.toolvisuals.client.hud.layout;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.input.GuiMovement;
import ru.he3hauka.toolvisuals.client.gui.input.MovementScreen;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.ExtraHud;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.layout.resize.HudResize;
import ru.he3hauka.toolvisuals.client.hud.layout.resize.HudScale;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class HudEditorScreen extends Screen implements MovementScreen {
    private final Screen parent;
    final Map<HudElement, HudPosition> positions = new EnumMap<>(HudElement.class);
    final Map<HudElement, Double> scales = new EnumMap<>(HudElement.class);
    boolean resetDefaults;
    private boolean grid = true, snap = true;
    public boolean samples=true;
    private HudElement selected, dragging;
    private HudPlacement.Bounds dragBounds;
    private float grabX, grabY;
    private Map<HudElement, HudPlacement.Bounds> bounds = Map.of();
    private UiViewport viewport;
    private List<VisualsButton> toolbarButtons=List.of();
    private static final int TOOLBAR_HEIGHT=77;
    private int toolbarX,toolbarY,toolbarWidth;
    private boolean draggingToolbar,toolbarMoved;
    private long frame;
    public HudEditorScreen(Screen parent) {
        super(UiI18n.tr("toolvisuals.hud.editor")); this.parent = parent;
        positions.putAll(ToolVisualsClient.CONFIG.hudPositions());
        scales.putAll(ToolVisualsClient.CONFIG.hudScales());
        net.minecraft.client.KeyMapping.releaseAll();
    }
    @Override protected void init() {
        saveToolbar();draggingToolbar=false;
        viewport = new UiViewport(width, height, minecraft.getWindow().getGuiScale());
        toolbarWidth=Math.min(560,viewport.width-16);
        var position=ToolVisualsClient.CONFIG.hudEditorToolbar();
        toolbarX=Math.round(HudFormats.position(viewport.width,toolbarWidth,position.x));
        toolbarY=Math.round(HudFormats.position(viewport.height,TOOLBAR_HEIGHT,position.y));
        int unit=(toolbarWidth-16)/6;
        toolbarButtons=List.of(button(unit,"grid",()->grid=!grid,()->grid),
                button(unit,"snap",()->snap=!snap,()->snap),
                button(unit,"samples",()->samples=!samples,()->samples),
                button(unit,"reset",()->{positions.clear();scales.clear();resetDefaults=true;selected=null;},null),
                button(unit,"cancel",()->minecraft.gui.setScreen(parent),null),
                button(unit,"done",this::saveAndClose,null));
        positionToolbar();
        frame = System.nanoTime();
    }
    private VisualsButton button(int w, String key, Runnable action, java.util.function.BooleanSupplier value) {
        return addWidget(new VisualsButton(0,0,w,30,UiI18n.tr("toolvisuals.hud." + key), VisualsButton.Kind.CHIP,
                null, Component.empty(), action, value));
    }
    private void positionToolbar() {
        int unit=(toolbarWidth-16)/6;
        for(int i=0;i<toolbarButtons.size();i++) {
            var button=toolbarButtons.get(i);button.setX(toolbarX+8+i*unit);button.setY(toolbarY+42);
        }
    }
    private void moveToolbar(float x,float y) {
        toolbarX=Math.round(Math.clamp(x,0,Math.max(0,viewport.width-toolbarWidth)));
        toolbarY=Math.round(Math.clamp(y,0,Math.max(0,viewport.height-TOOLBAR_HEIGHT)));
        toolbarMoved=true;positionToolbar();
    }
    private void saveToolbar() {
        if(!toolbarMoved||viewport==null)return;
        ToolVisualsClient.CONFIG.setHudEditorToolbar(HudPosition.fromPixels(toolbarX,toolbarY,viewport.width,viewport.height,toolbarWidth,TOOLBAR_HEIGHT));
        toolbarMoved=false;
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) { g.fill(0, 0, width, height, 0x70070810); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
        var palette = new UiPalette(ToolVisualsClient.CONFIG.lightTheme() ? 1 : 0);
        float dt = Math.clamp((System.nanoTime() - frame) / 1_000_000_000f, 0, .1f); frame = System.nanoTime();
        if (grid) {
            int step = Math.max(2, Math.round(8 * viewport.scale));
            for (int x = 0; x < width; x += step) g.fill(x, 0, x + 1, height, 0x0bffffff);
            for (int y = 0; y < height; y += step) g.fill(0, y, width, y + 1, 0x0bffffff);
            g.fill(width / 2, 0, width / 2 + 1, height, 0x407d68cc);
            g.fill(0, height / 2, width, height / 2 + 1, 0x407d68cc);
        }
        HudPlacement.drawPreview(g); bounds = HudPlacement.bounds();
        g.nextStratum();
        for (var entry : bounds.entrySet()) {
            var element = entry.getKey(); var box = entry.getValue();
            boolean focus = element == selected || box.contains(mx, my);
            int color = UiPaint.alpha(focus ? palette.accent : 0xffb6a8dc, focus ? 1 : .45f);
            g.outline(Math.round(box.x) - 1, Math.round(box.y) - 1, Math.round(box.width) + 2, Math.round(box.height) + 2, color);
            if (focus) {
                g.pose().pushMatrix(); g.pose().translate(box.x, Math.max(0, box.y - 19 * viewport.scale)); g.pose().scale(viewport.scale, viewport.scale);
                String text = UiI18n.tr("toolvisuals.hud.element." + element.id).getString();
                if(element==selected&&HudScale.enabled(element,ToolVisualsClient.CONFIG))text+=" · "+Math.round(scales.getOrDefault(element,1d)*100)+"%";
                if (!ToolVisualsClient.CONFIG.active(element.module)) text += " · " + UiI18n.tr("toolvisuals.ui.off").getString();
                UiPaint.rounded(g, 0, 0, UiPaint.textWidth(g, text, .75f) + 12, 18, 4, palette.panel);
                UiPaint.text(g, text, 6, 2, .75f, palette.text, 1); g.pose().popMatrix();
            }
        }
        g.pose().pushMatrix(); g.pose().scale(viewport.scale, viewport.scale);
        float mouseX=mx/viewport.scale,mouseY=my/viewport.scale;
        boolean grip=mouseX>=toolbarX&&mouseX<toolbarX+toolbarWidth&&mouseY>=toolbarY&&mouseY<toolbarY+37;
        UiPaint.rounded(g,toolbarX+toolbarWidth/2f-18,toolbarY+2,36,4,2,grip||draggingToolbar?palette.accent:palette.muted);
        UiPaint.rounded(g,toolbarX,toolbarY+37,toolbarWidth,40,10,palette.panel);
        for (var button : toolbarButtons) {
            button.frame(palette, 1, dt); button.extractRenderState(g, Math.round(mx / viewport.scale), Math.round(my / viewport.scale), partial);
        }
        String hint = UiI18n.tr("toolvisuals.hud.drag_hint").getString();
        float hintWidth = Math.min(toolbarWidth-16,UiPaint.textWidth(g,hint,.75f)+20);
        float hintX=toolbarX+(toolbarWidth-hintWidth)/2;
        UiPaint.rounded(g,hintX,toolbarY+10,hintWidth,22,6,palette.panel);
        UiPaint.centered(g,UiPaint.fit(g,Component.literal(hint),hintWidth-12,.75f),hintX,toolbarY+10,hintWidth,22,.75f,palette.muted,1);
        g.pose().popMatrix();
    }
    private MouseButtonEvent adjusted(MouseButtonEvent event) {
        return new MouseButtonEvent(event.x() / viewport.scale, event.y() / viewport.scale, event.buttonInfo());
    }
    @Override public boolean mouseClicked(MouseButtonEvent event, boolean twice) {
        var local=adjusted(event);
        if(local.x()>=toolbarX&&local.x()<toolbarX+toolbarWidth&&local.y()>=toolbarY&&local.y()<toolbarY+TOOLBAR_HEIGHT) {
            if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&local.y()<toolbarY+37) {
                draggingToolbar=true;dragging=null;grabX=(float)local.x()-toolbarX;grabY=(float)local.y()-toolbarY;return true;
            }
            super.mouseClicked(local,twice);return true;
        }
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;
        var element=ru.he3hauka.toolvisuals.client.hud.layout.HudDragging.pick(bounds,value->true,event.x(),event.y());
        if(element!=null){
            var box = bounds.get(element);
            selected = dragging = element; dragBounds = box;
            grabX = (float)event.x() - box.x; grabY = (float)event.y() - box.y; return true;
        }
        selected = null; return true;
    }
    private void move(float x, float y) {
        float step = 8 * viewport.scale;
        if (snap) { x = Math.round(x / step) * step; y = Math.round(y / step) * step; }
        positions.put(selected, HudPosition.fromPixels(x, y, width, height, dragBounds.width, dragBounds.height));
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if(draggingToolbar){var local=adjusted(event);moveToolbar((float)local.x()-grabX,(float)local.y()-grabY);return true;}
        if (dragging == null) return false;
        move((float)event.x() - grabX, (float)event.y() - grabY); return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if(draggingToolbar){draggingToolbar=false;saveToolbar();return true;}
        if (dragging != null) { dragging = null; return true; }
        return super.mouseReleased(adjusted(event));
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        float localX=(float)(x/viewport.scale),localY=(float)(y/viewport.scale);
        if(localX>=toolbarX&&localX<toolbarX+toolbarWidth&&localY>=toolbarY&&localY<toolbarY+TOOLBAR_HEIGHT)return super.mouseScrolled(x/viewport.scale,y/viewport.scale,horizontal,vertical);
        if(selected==null||!bounds.containsKey(selected)||!HudScale.enabled(selected,ToolVisualsClient.CONFIG)||vertical==0||!Double.isFinite(vertical))return false;
        var box=bounds.get(selected);
        if(dragging==selected)box=HudResize.positioned(box,positions.get(selected),width,height);
        if(!box.contains(x,y))return false;
        var change=HudResize.change(selected,box,scales.getOrDefault(selected,1d),vertical,width,height,0);
        if(change!=null){
            scales.put(selected,change.scale());positions.put(selected,change.position());dragBounds=change.bounds();
            HudPlacement.updateBounds(selected,change.bounds());bounds=HudPlacement.bounds();
            if(dragging==selected){grabX=(float)x-dragBounds.x;grabY=(float)y-dragBounds.y;}
        }
        return true;
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE || ToolVisualsClient.openMenuKey.matches(event)) { saveAndClose(); return true; }
        if (GuiMovement.movementKey(minecraft.options,event)) return true;
        if (selected != null && bounds.containsKey(selected)) {
            var box = bounds.get(selected); dragBounds = box;
            float step = snap ? 8 * viewport.scale : viewport.scale;
            switch (event.key()) {
                case InputConstants.KEY_LEFT -> move(box.x - step, box.y);
                case InputConstants.KEY_RIGHT -> move(box.x + step, box.y);
                case InputConstants.KEY_UP -> move(box.x, box.y - step);
                case InputConstants.KEY_DOWN -> move(box.x, box.y + step);
                default -> { return super.keyPressed(event); }
            }
            return true;
        }
        return super.keyPressed(event);
    }
    private void saveAndClose() { ToolVisualsClient.CONFIG.applyHudLayout(positions,scales,resetDefaults); minecraft.gui.setScreen(parent); }
    @Override public void onClose() { saveAndClose(); }
    @Override public void removed() { saveToolbar();super.removed(); ExtraHud.reset(); }
}
