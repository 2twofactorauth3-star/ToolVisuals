package ru.he3hauka.toolvisuals.client.hud.layout;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2f;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.hud.HudFormats;
import ru.he3hauka.toolvisuals.client.hud.VisualHud;
import ru.he3hauka.toolvisuals.client.hud.info.InfoHudPart;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;

public final class HudPlacement {
    public static final class Bounds {
        public final float x, y, width, height;
        public Bounds(float x, float y, float width, float height) { this.x = x; this.y = y; this.width = width; this.height = height; }
        public boolean contains(double x, double y) { return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height; }
    }
    private static final Map<HudElement, Bounds> BOUNDS = new EnumMap<>(HudElement.class);
    private static boolean preview;
    private HudPlacement() {}
    public static boolean preview() { return preview; }
    public static boolean samples(){return preview&&Minecraft.getInstance().gui.screen() instanceof HudEditorScreen editor&&editor.samples;}
    public static boolean show(VisualModule module) { return preview || ToolVisualsClient.CONFIG.active(module); }
    public static Map<HudElement, Bounds> bounds() { return Map.copyOf(BOUNDS); }
    public static void updateBounds(HudElement element,Bounds bounds){BOUNDS.put(element,bounds);}
    public static void beginFrame(){if(!preview)BOUNDS.clear();}
    public static void drawPreview(GuiGraphicsExtractor graphics) {
        BOUNDS.clear(); preview = true;
        try { VisualHud.draw(graphics);ru.he3hauka.toolvisuals.client.utility.bossbars.BossBars.preview(graphics); } finally { preview = false; }
    }
    public static float value(VisualSetting setting) {
        if (Minecraft.getInstance().gui.screen() instanceof HudEditorScreen editor && editor.resetDefaults)
            for (var element : HudElement.values()) if (element.xSetting == setting || element.ySetting == setting) return (float)setting.initial;
        return ToolVisualsClient.CONFIG.number(setting);
    }
     
    public static void place(GuiGraphicsExtractor graphics, HudElement element, float x, float y, float width, float height) {
        if(!preview)ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.hud(element.module);
        var client = Minecraft.getInstance();
        var positions = client.gui.screen() instanceof HudEditorScreen editor ? editor.positions : ToolVisualsClient.CONFIG.hudPositions();
        var position = positions.get(element);
        if(client.gui.screen() instanceof ru.he3hauka.toolvisuals.client.hud.layout.ChatHudAccess chat)position=chat.toolvisuals$hudDragging().position(element,position);
        float sx = graphics.pose().m00(), sy = graphics.pose().m11();
        double factor=client.gui.screen() instanceof HudEditorScreen editor?editor.scales.getOrDefault(element,1d):ToolVisualsClient.CONFIG.hudScale(element);
        float scaledWidth=(float)(width*factor),scaledHeight=(float)(height*factor);
        float screenWidth = graphics.guiWidth() / sx, screenHeight = graphics.guiHeight() / sy;
        float placedX = position == null ? x : HudFormats.position(screenWidth, scaledWidth, position.x);
        float placedY = position == null ? y : HudFormats.position(screenHeight, scaledHeight, position.y);
         
        placedX = Math.clamp(placedX, 0, Math.max(0, screenWidth - scaledWidth));
        float bottom = screenHeight - scaledHeight;
        if (client.gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen && InfoHudPart.forElement(element) != null)
            bottom -= 18 / sy;
        placedY = Math.clamp(placedY, 0, Math.max(0, bottom));
        if (preview||client.gui.screen() instanceof net.minecraft.client.gui.screens.ChatScreen&&ru.he3hauka.toolvisuals.client.hud.layout.HudDragging.enabled(element,ToolVisualsClient.CONFIG)) {
            var point = graphics.pose().transformPosition(placedX, placedY, new Vector2f());
            updateBounds(element, new Bounds(point.x, point.y, scaledWidth * sx, scaledHeight * sy));
        }
        graphics.pose().pushMatrix();graphics.pose().translate(placedX-x*(float)factor,placedY-y*(float)factor);graphics.pose().scale((float)factor,(float)factor);
    }
}
