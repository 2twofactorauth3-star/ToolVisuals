package ru.he3hauka.toolvisuals.client.config.ui;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.config.ConfigProfiles;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public final class ConfigScreen extends Screen {
    private static final int PITCH = 56;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
    private final Screen parent;
    private final ConfigProfiles store = ToolVisualsClient.PROFILES;
    private final List<VisualsButton> controls = new ArrayList<>();
    private final List<ProfileRow> rows = new ArrayList<>();
    private final Motion scroll = new Motion(0);
    private UiViewport viewport;
    private UiPalette palette = new UiPalette(0);
    private java.nio.file.Path downloadsDirectory;
    private EditBox name;
    private VisualsButton create, load, overwrite, rename, delete, export, download, defaults;
    private String draft = "", selectedId, pending = "";
    private Component status = Component.empty();
    private boolean error, draggingScroll;
    private int px, py, pw, ph, listY, listBottom;
    private float scrollTarget, scrollGrab;
    private long lastFrame;

    public ConfigScreen(Screen parent) { super(tr("title")); this.parent = parent; }
    private static Component tr(String key, Object... args) { return UiI18n.tr("toolvisuals.configs." + key, args); }
    @Override protected void init() {
        controls.clear(); rows.clear();
        String downloads = org.lwjgl.sdl.SDLFileSystem.SDL_GetUserFolder(org.lwjgl.sdl.SDLFileSystem.SDL_FOLDER_DOWNLOADS);
        downloadsDirectory = downloads == null || downloads.isBlank() ? java.nio.file.Path.of(System.getProperty("user.home"), "Downloads") : java.nio.file.Path.of(downloads);
        viewport = new UiViewport(width, height, minecraft.getWindow().getGuiScale());
        pw = Math.min(700, viewport.width - 32); ph = Math.min(484, viewport.height - 32);
        px = (viewport.width - pw) / 2; py = (viewport.height - ph) / 2;
        name = addWidget(new EditBox(UiFont.get(), px + 30, py + 76, pw - 174, 32, tr("name")) {
            @Override public void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
                if (getValue().isEmpty() && !isFocused()) {
                    line(g, tr("name"), getX(), getY(), getWidth(), getHeight(), .9f, palette.muted); return;
                }
                g.pose().pushMatrix(); g.pose().translate(0, UiFont.centeredTextOffset(getHeight()));
                super.extractWidgetRenderState(g, mx, my, partial); g.pose().popMatrix();
            }
        });
        name.setBordered(false); name.setTextShadow(false); name.setMaxLength(96); name.setValue(draft);
        name.setResponder(value -> { draft = value; pending = ""; updateActions(); });
        create = control(px + pw - 134, py + 76, 114, 32, "create", this::create);
        control(px + pw - 40, py + 12, 26, 26, "back", this::onClose);
        control(px + pw - 184, py + 12, 132, 26, "folder", this::openFolder);
        int gap = 6, buttonW = (pw - 40 - gap * 3) / 4, y = py + ph - 134;
        load = control(px + 20, y, buttonW, 32, "load", this::load);
        overwrite = control(px + 20 + (buttonW + gap), y, buttonW, 32, "overwrite", this::overwrite);
        rename = control(px + 20 + (buttonW + gap) * 2, y, buttonW, 32, "rename", this::rename);
        delete = control(px + 20 + (buttonW + gap) * 3, y, buttonW, 32, "delete", this::delete);
        y += 42;
        control(px + 20, y, buttonW, 30, "import", this::importClipboard);
        export = control(px + 20 + (buttonW + gap), y, buttonW, 30, "export", this::exportClipboard);
        download = control(px + 20 + (buttonW + gap) * 2, y, buttonW, 30, "download", this::download);
        defaults = control(px + 20 + (buttonW + gap) * 3, y, buttonW, 30, "defaults", this::defaults);
        listY = py + 124; listBottom = py + ph - 148;
        refresh(); lastFrame = System.nanoTime();
    }
    private VisualsButton control(int x, int y, int w, int h, String key, Runnable action) {
        var button = addWidget(new VisualsButton(x, y, w, h, tr(key), key.equals("back") ? ICON : CHIP,
                key.equals("back") ? UiPaint.Icon.BACK : null, Component.empty(), action, null));
        controls.add(button); return button;
    }
    private ConfigProfiles.Profile selected() { return store.profiles().stream().filter(p -> p.id.equals(selectedId)).findFirst().orElse(null); }
    private void select(ConfigProfiles.Profile profile) { selectedId = profile.id; pending = ""; name.setValue(profile.name); updateActions(); }
    private void refresh() {
        try { store.refresh(); } catch (IOException ex) { ClientErrors.report("Refresh config library",ex);feedback("io_error", true); }
        rows.forEach(this::removeWidget); rows.clear();
        if (selected() == null) selectedId = store.profiles().isEmpty() ? null : store.profiles().getFirst().id;
        for (var profile : store.profiles()) rows.add(addWidget(new ProfileRow(profile)));
        if (getFocused() != null && !children().contains(getFocused())) setFocused(null);
        scrollTarget = Math.clamp(scrollTarget, 0, maxScroll()); scroll.snap(scrollTarget); positionRows(); updateActions();
    }
    private void updateActions() {
        if (create == null || defaults == null) return;
        var profile = selected(); boolean chosen = profile != null;
        create.active = store.profiles().size() < ConfigProfiles.LIMIT && store.nameAvailable(draft, "");
        load.active = overwrite.active = delete.active = export.active = download.active = chosen;
        rename.active = chosen && store.nameAvailable(draft, profile.id) && !draft.strip().equals(profile.name);
        overwrite.setMessage(tr(pending.equals("overwrite") ? "confirm_overwrite" : "overwrite"));
        delete.setMessage(tr(pending.equals("delete") ? "confirm_delete" : "delete"));
        defaults.setMessage(tr(pending.equals("defaults") ? "confirm_defaults" : "defaults"));
    }
    private void feedback(String key, boolean failed, Object... args) {
        status = tr(key, args); error = failed;
        if(!key.equals("confirm_hint"))ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.notice();
    }
    private boolean confirm(String operation) {
        if (pending.equals(operation)) { pending = ""; return true; }
        pending = operation; feedback("confirm_hint", false); updateActions(); return false;
    }
    @FunctionalInterface private interface Operation { void run() throws IOException; }
    private void perform(Operation operation) {
        try { operation.run(); } catch (IOException ex) { ClientErrors.report("Config profile action",ex);feedback("io_error", true); }
        catch (RuntimeException ex) { ClientErrors.report("Apply config profile",ex);feedback("invalid", true); }
        pending = ""; refresh();
    }
    private void create() {
        perform(() -> { var profile = store.create(draft, ToolVisualsClient.CONFIG); selectedId = profile.id; feedback("saved", false, profile.name); });
    }
    private void load() {
        if (selected() == null) return;
        perform(() -> { var p = selected(); store.apply(p.id, ToolVisualsClient.CONFIG); Optimizations.update(ToolVisualsClient.CONFIG); feedback("loaded", false, p.name); });
    }
    private void overwrite() {
        if (selected() == null || !confirm("overwrite")) return;
        perform(() -> { var p = store.overwrite(selectedId, ToolVisualsClient.CONFIG); feedback("saved", false, p.name); });
    }
    private void rename() {
        if (selected() == null) return;
        perform(() -> { var p = store.rename(selectedId, draft); feedback("renamed", false, p.name); });
    }
    private void delete() {
        if (selected() == null || !confirm("delete")) return;
        perform(() -> { String old = selected().name; store.delete(selectedId); feedback("removed", false, old); });
    }
    private void defaults() {
        if (!confirm("defaults")) return;
        perform(() -> { ToolVisualsClient.CONFIG.resetVisuals(); Optimizations.update(ToolVisualsClient.CONFIG); feedback("reset", false); });
    }
    private void exportClipboard() {
        if (selected() == null) return;
        perform(() -> { minecraft.keyboardHandler.setClipboard(store.exportJson(selectedId)); feedback("copied", false); });
    }
    private void importClipboard() {
        perform(() -> {
            var p = store.importJson(minecraft.keyboardHandler.getClipboard(), draft.isBlank() ? tr("imported_name").getString() : draft);
            selectedId = p.id; feedback("imported", false, p.name);
        });
    }
    private void openFolder() {
        perform(() -> {
            java.nio.file.Files.createDirectories(store.directory);
            com.mojang.blaze3d.Blaze3D.openPath(store.directory);
            feedback("folder_opened", false);
        });
    }
    private void download() {
        if (selected() == null) return;
        perform(() -> {
            var file = store.download(selectedId, downloadsDirectory);
            feedback("downloaded", false, file.getFileName());
        });
    }
    private float maxScroll() { return Math.max(0, rows.size() * PITCH - 4 - (listBottom - listY)); }
    private void positionRows() {
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i); row.setY(listY + i * PITCH - Math.round(scroll.value()));
            row.visible = row.getY() < listBottom && row.getY() + 52 > listY;
        }
    }
    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) {
        if (minecraft.level == null) extractPanorama(g, partial);
        g.fill(0, 0, width, height, 0x88070910);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
        if (width != g.guiWidth() || height != g.guiHeight()) { resize(g.guiWidth(), g.guiHeight()); return; }
        long now = System.nanoTime(); float dt = Math.clamp((now - lastFrame) / 1e9f, 0, 1); lastFrame = now;
        scroll.step(scrollTarget, dt, 22); positionRows(); palette = new UiPalette(ToolVisualsClient.CONFIG.lightTheme() ? 1 : 0);
        mx = Math.round(mx / viewport.scale); my = Math.round(my / viewport.scale);
        g.pose().pushMatrix(); g.pose().scale(viewport.scale, viewport.scale);
        UiPaint.shadow(g, px, py, pw, ph, 12, 1); UiPaint.rounded(g, px - 1, py - 1, pw + 2, ph + 2, 13, palette.border);
        UiPaint.rounded(g, px, py, pw, ph, 12, palette.panel);
        line(g, getTitle(), px + 20, py + 10, pw - 220, 30, 1.1f, palette.text);
        line(g, tr("subtitle", store.profiles().size(), ConfigProfiles.LIMIT), px + 20, py + 46, pw - 40, 22, .8f, palette.muted);
        UiPaint.rounded(g, px + 20, py + 76, pw - 160, 32, 6, palette.field);
        name.setTextColor(palette.text); name.extractRenderState(g, mx, my, partial);
        g.enableScissor(px + 20, listY, px + pw - 20, listBottom);
        for (var row : rows) row.extractRenderState(g, mx, my >= listY && my < listBottom ? my : -1000, partial);
        if (rows.isEmpty()) line(g, tr("empty"), px + 32, listY, pw - 64, 44, .9f, palette.muted);
        g.disableScissor();
        if (maxScroll() > 0) {
            float track = listBottom - listY, thumb = Math.max(18, track * track / (track + maxScroll()));
            UiPaint.rounded(g, px + pw - 23, listY, 3, track, 1.5f, palette.track);
            UiPaint.rounded(g, px + pw - 23, listY + (track - thumb) * scroll.value() / maxScroll(), 3, thumb, 1.5f, palette.accent);
        }
        for (var button : controls) { button.frame(palette, 1, dt); button.extractRenderState(g, mx, my, partial); }
        line(g, status, px + 20, py + ph - 51, pw - 40, 20, .8f, error ? 0xffff879c : palette.accentText);
        line(g, tr("scope"), px + 20, py + ph - 29, pw - 40, 18, .7f, palette.muted);
        g.pose().popMatrix();
    }
    private void line(GuiGraphicsExtractor g, Component text, float x, float y, float w, float h, float scale, int color) {
        UiPaint.baseline(g, UiPaint.fit(g, text, w, scale), x, UiPaint.textBaseline(g, y, h, scale), scale, color, 1);
    }
    private final class ProfileRow extends Button {
        public final ConfigProfiles.Profile profile;
        ProfileRow(ConfigProfiles.Profile profile) {
            super(px + 20, 0, pw - 48, 52, Component.literal(profile.name), b -> ConfigScreen.this.select(profile), DEFAULT_NARRATION); this.profile = profile;
        }
        @Override protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float partial) {
            boolean chosen = profile.id.equals(selectedId);
            UiPaint.rounded(g, getX(), getY(), getWidth(), getHeight(), 7, chosen ? palette.hover : palette.raised);
            line(g, Component.literal(profile.name), getX() + 12, getY() + 3, getWidth() - 42, 26, 1, palette.text);
            line(g, tr("detail", profile.enabledModules, DATE.format(Instant.ofEpochMilli(profile.updatedAt))), getX() + 12, getY() + 30, getWidth() - 30, 18, .75f, palette.muted);
            if (chosen) UiPaint.icon(g, UiPaint.Icon.CHECK, getX() + getWidth() - 27, getY() + 10, 14, palette.accentText, 1);
        }
    }
    private MouseButtonEvent adjusted(MouseButtonEvent e) { return new MouseButtonEvent(e.x() / viewport.scale, e.y() / viewport.scale, e.buttonInfo()); }
    @Override public boolean mouseClicked(MouseButtonEvent raw, boolean twice) {
        var e = adjusted(raw);
        if (e.button() == InputConstants.MOUSE_BUTTON_LEFT && maxScroll() > 0 && e.x() >= px + pw - 28 && e.x() <= px + pw - 16 && e.y() >= listY && e.y() < listBottom) {
            float track = listBottom - listY, thumb = Math.max(18, track * track / (track + maxScroll()));
            float top = listY + (track - thumb) * scroll.value() / maxScroll();
            scrollGrab = e.y() >= top && e.y() < top + thumb ? (float)e.y() - top : thumb / 2;
            draggingScroll = true; dragScroll(e.y()); return true;
        }
        if (e.y() < listY || e.y() >= listBottom) rows.forEach(row -> row.visible = false);
        boolean result = super.mouseClicked(e, twice); positionRows(); return result;
    }
    private void dragScroll(double y) {
        float track = listBottom - listY, thumb = Math.max(18, track * track / (track + maxScroll()));
        scrollTarget = Math.clamp((float)(y - listY - scrollGrab) / Math.max(1, track - thumb) * maxScroll(), 0, maxScroll());
        scroll.snap(scrollTarget); positionRows();
    }
    @Override public void mouseMoved(double x, double y) { super.mouseMoved(x / viewport.scale, y / viewport.scale); }
    @Override public boolean mouseReleased(MouseButtonEvent e) { draggingScroll = false; return super.mouseReleased(adjusted(e)); }
    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (draggingScroll) { dragScroll(adjusted(e).y()); return true; }
        return super.mouseDragged(adjusted(e), dx / viewport.scale, dy / viewport.scale);
    }
    @Override public boolean mouseScrolled(double x, double y, double h, double v) {
        if (y / viewport.scale >= listY && y / viewport.scale < listBottom) { scrollTarget = Math.clamp(scrollTarget - (float)v * 42, 0, maxScroll()); return true; }
        return false;
    }
    @Override public boolean keyPressed(KeyEvent e) {
        if (e.key() == InputConstants.KEY_RETURN && name.isFocused() && create.active) { create(); return true; }
        if ((e.key() == InputConstants.KEY_PAGEUP || e.key() == InputConstants.KEY_PAGEDOWN) && !name.isFocused()) {
            scrollTarget = Math.clamp(scrollTarget + (e.key() == InputConstants.KEY_PAGEUP ? -1 : 1) * (listBottom - listY), 0, maxScroll()); return true;
        }
        return super.keyPressed(e);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
