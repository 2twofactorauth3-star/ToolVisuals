package ru.he3hauka.toolvisuals.client.navigation.gps.ui;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.navigation.gps.Gps;
import ru.he3hauka.toolvisuals.client.navigation.gps.GpsMark;
import ru.he3hauka.toolvisuals.client.navigation.gps.GpsTarget;
import ru.he3hauka.toolvisuals.client.waypoint.Waypoints;

public final class GpsScreen extends VisualEditorScreen {
    private static final int PAGE_SIZE = 6;
    private String name = "", world = "", x = "0", y = "64", z = "0", status = "";
    private UUID editing;
    private int page;
    private VisualsButton save;

    public GpsScreen(Screen parent) { super(parent, text("title")); }
    private static Component text(String key, Object... args) { return UiI18n.tr("toolvisuals.gps." + key, args); }
    private List<GpsMark> entries() {
        return Waypoints.context().isEmpty() ? ToolVisualsClient.GPS_POINTS.all() : ToolVisualsClient.GPS_POINTS.entries(Waypoints.context());
    }
    private GpsMark edited() { return ToolVisualsClient.GPS_POINTS.all().stream().filter(mark -> mark.id().equals(editing)).findFirst().orElse(null); }

    @Override protected void init() {
        frame(700, 550);
        field(30, 76, 306, name, 40, value -> { name = value; validate(); });
        field(370, 76, 300, world, 128, value -> { world = value; validate(); });
        field(30, 126, 192, x, 20, value -> { x = value; validate(); });
        field(254, 126, 192, y, 20, value -> { y = value; validate(); });
        field(478, 126, 192, z, 20, value -> { z = value; validate(); });
        save = button(24, 170, 210, 32, text(editing == null ? "add" : "save"), UiPaint.Icon.CHECK, this::save, null);
        button(244, 170, 210, 32, text("new"), UiPaint.Icon.PLUS, () -> { editing = null; name = ""; rebuildWidgets(); }, null);
        button(464, 170, 212, 32, text("here"), null, () -> {
            if (minecraft.player != null) { x = number(minecraft.player.getX()); y = number(minecraft.player.getY()); z = number(minecraft.player.getZ()); rebuildWidgets(); }
        }, null);
        var all = entries();
        page = Math.clamp(page, 0, Math.max(0, (all.size() - 1) / PAGE_SIZE));
        button(360, 212, 152, 28, text("select_all"), null, () -> change(() -> ToolVisualsClient.GPS_POINTS.selectAll(Waypoints.context(), true)), null);
        button(524, 212, 152, 28, text("clear"), null, () -> change(() -> ToolVisualsClient.GPS_POINTS.selectAll(Waypoints.context(), false)), null);
        for (int index = page * PAGE_SIZE; index < Math.min(all.size(), (page + 1) * PAGE_SIZE); index++) {
            var mark = all.get(index); int top = 252 + (index % PAGE_SIZE) * 36;
            String label = mark.name() + "  " + number(mark.target().x()) + " " + number(mark.target().y()) + " " + number(mark.target().z());
            button(24, top, 338, 30, Component.literal(label), null, () -> edit(mark), () -> mark.id().equals(editing));
            button(372, top, 44, 30, text(mark.active() ? "on" : "off"), null, () -> change(() -> ToolVisualsClient.GPS_POINTS.toggle(mark.id())), mark::active);
            button(426, top, 142, 30, text("only"), null, () -> change(() -> ToolVisualsClient.GPS_POINTS.selectOnly(mark.context(), mark.id())), null);
            button(578, top, 98, 30, text("delete"), UiPaint.Icon.CLOSE, () -> change(() -> { ToolVisualsClient.GPS_POINTS.remove(mark.id()); if (mark.id().equals(editing)) editing = null; }), null);
        }
        button(24, 480, 46, 30, Component.literal("<"), null, () -> { page--; rebuildWidgets(); }, null).active = page > 0;
        button(80, 480, 46, 30, Component.literal(">"), null, () -> { page++; rebuildWidgets(); }, null).active = (page + 1) * PAGE_SIZE < all.size();
        button(534, 480, 142, 30, tr("back"), UiPaint.Icon.BACK, this::onClose, null);
        validate();
    }

    private static String number(double value) { return String.format(Locale.ROOT, "%.1f", value); }
    private GpsTarget target() { return new GpsTarget(Double.parseDouble(x.replace(',', '.')), Double.parseDouble(y.replace(',', '.')), Double.parseDouble(z.replace(',', '.')), world.strip()); }
    private void validate() {
        if (save == null) return;
        try { target(); save.active = !name.isBlank() && name.strip().length() <= 40 && (!Waypoints.context().isEmpty() || edited() != null); }
        catch (IllegalArgumentException error) { save.active = false; }
    }
    private void edit(GpsMark mark) {
        editing = mark.id(); name = mark.name(); world = mark.target().world();
        x = number(mark.target().x()); y = number(mark.target().y()); z = number(mark.target().z());
        rebuildWidgets();
    }
    private void save() {
        change(() -> {
            var old = edited();
            UUID id = old == null ? UUID.randomUUID() : old.id();
            ToolVisualsClient.GPS_POINTS.put(new GpsMark(id, old == null ? Waypoints.context() : old.context(), name, target(), old == null || old.active()), false);
            editing = id;
        });
    }
    @FunctionalInterface private interface Change { void run() throws IOException; }
    private void change(Change change) {
        try { change.run(); status = text("saved").getString(); Gps.tick(minecraft); rebuildWidgets(); }
        catch (IOException | IllegalArgumentException | IllegalStateException error) { status = text("save_error").getString(); }
    }
    @Override protected void paint(GuiGraphicsExtractor graphics, int mx, int my) {
        label(graphics, "name", 24, 54, 310); label(graphics, "world", 364, 54, 310);
        label(graphics, "x", 24, 105, 200); label(graphics, "y", 248, 105, 200); label(graphics, "z", 472, 105, 200);
        long active = entries().stream().filter(GpsMark::active).count();
        UiPaint.text(graphics, text("selected", active, entries().size()), px + 24, py + 216, .8f, palette.muted, 1);
        UiPaint.centered(graphics, status.isEmpty() ? text("help").getString() : status, px + 24, py + 517, 652, 22, .72f, palette.muted, 1);
        if (entries().isEmpty()) UiPaint.centered(graphics, text("empty").getString(), px + 24, py + 290, 652, 80, .9f, palette.muted, 1);
    }
    private void label(GuiGraphicsExtractor graphics, String key, int x, int y, int width) {
        UiPaint.text(graphics, UiPaint.fit(graphics, text(key), width, .72f), px + x, py + y, .72f, palette.muted, 1);
    }
}
