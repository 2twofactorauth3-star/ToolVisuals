package ru.he3hauka.toolvisuals.client.sound.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.gui.dialog.FileDialogs;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.Channel;
import ru.he3hauka.toolvisuals.client.sound.SoundPreset;
import ru.he3hauka.toolvisuals.client.sound.library.AudioDecoder;
import ru.he3hauka.toolvisuals.client.sound.library.CustomSounds;
import ru.he3hauka.toolvisuals.client.sound.library.SoundIds;

public final class SoundLibraryScreen extends VisualEditorScreen {
    private static final int PAGE_SIZE = 6;
    private String id = "", status = "";
    private boolean busy;
    private int page;
    private VisualsButton importButton;

    public SoundLibraryScreen(Screen parent) { super(parent, text("title")); }
    private static Component text(String key, Object... values) { return UiI18n.tr("toolvisuals.sound_library." + key, values); }
    @Override protected void init() {
        frame(720, 510);
        field(30, 78, 440, id, 32, value -> { id = value; validate(); });
        importButton = button(494, 78, 202, 28, text("import"), UiPaint.Icon.PLUS, this::choose, null);
        var entries = ToolVisualsClient.SOUNDS.entries();
        page = Math.clamp(page, 0, Math.max(0, (entries.size() - 1) / PAGE_SIZE));
        for (int index = page * PAGE_SIZE; index < Math.min(entries.size(), (page + 1) * PAGE_SIZE); index++) {
            var entry = entries.get(index); int top = 178 + (index % PAGE_SIZE) * 38;
            button(24, top, 180, 32, Component.literal(entry.id()), null, () -> preview(entry.id()), null);
            button(214, top, 70, 32, text("preview"), null, () -> preview(entry.id()), null);
            int column = 0;
            for (var channel : Channel.values()) {
                button(294 + column++ * 102, top, 92, 32, text(channel.name().toLowerCase(java.util.Locale.ROOT)), null,
                        () -> assign(channel, entry.id()), () -> entry.id().equals(ToolVisualsClient.SOUNDS.selected(channel))
                                && (int)ToolVisualsClient.CONFIG.value(kind(channel)) == SoundPreset.CUSTOM);
            }
            button(610, top, 86, 32, text("delete"), UiPaint.Icon.CLOSE, () -> change(() -> ToolVisualsClient.SOUNDS.remove(entry.id()), true), null);
        }
        button(24, 424, 46, 30, Component.literal("<"), null, () -> { page--; rebuildWidgets(); }, null).active = page > 0;
        button(80, 424, 46, 30, Component.literal(">"), null, () -> { page++; rebuildWidgets(); }, null).active = (page + 1) * PAGE_SIZE < entries.size();
        button(554, 424, 142, 30, tr("back"), UiPaint.Icon.BACK, this::onClose, null);
        validate();
    }
    private void validate() { if (importButton != null) importButton.active = !busy && ToolVisualsClient.SOUNDS.availableId(id); }
    private void choose() {
        if (busy || !ToolVisualsClient.SOUNDS.availableId(id)) return;
        String requested = SoundIds.require(id);
        FileDialogs.choose(false, text("import").getString(), "OGG Vorbis / WAV / AIFF / AU", AudioDecoder.EXTENSIONS, "",
                path -> importFile(requested, path), () -> status = text("error").getString());
    }
    private void importFile(String requested, Path path) {
        busy = true; status = text("loading").getString(); validate();
        CompletableFuture.supplyAsync(() -> {
            try { return ToolVisualsClient.SOUNDS.importSound(requested, AudioDecoder.decode(path)); }
            catch (IOException | RuntimeException error) { throw new CompletionException(error); }
        }).whenComplete((entry, failure) -> minecraft.execute(() -> {
            busy = false;
            if (failure != null) {
                Throwable cause = failure instanceof CompletionException && failure.getCause() != null ? failure.getCause() : failure;
                ClientErrors.report("Import custom audio " + requested, cause);
                status = text("error").getString();
            } else { status = text("imported", entry.id()).getString(); id = ""; minecraft.getSoundManager().reload(); }
            rebuildWidgets();
        }));
    }
    private void preview(String id) {
        if (!ToolVisualsClient.CONFIG.available(ru.he3hauka.toolvisuals.client.module.VisualModule.SOUND_CUSTOMIZER)) return;
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(CustomSounds.event(id), ToolVisualsClient.CONFIG.number(VisualSetting.SOUND_PITCH), .8f));
    }
    private void assign(Channel channel, String id) {
        change(() -> { ToolVisualsClient.SOUNDS.assign(channel, id); ToolVisualsClient.CONFIG.setValue(kind(channel), SoundPreset.CUSTOM); }, false);
    }
    private static VisualSetting kind(Channel channel) {
        return switch (channel) { case HIT -> VisualSetting.SOUND_HIT_KIND; case MODULE -> VisualSetting.SOUND_MODULE_KIND; case NOTICE -> VisualSetting.SOUND_NOTICE_KIND; };
    }
    @FunctionalInterface private interface Change { void run() throws IOException; }
    private void change(Change change, boolean reload) {
        try { change.run(); status = text("saved").getString(); if (reload) minecraft.getSoundManager().reload(); rebuildWidgets(); }
        catch (IOException | RuntimeException error) { ClientErrors.report("Update custom sound library", error); status = text("error").getString(); }
    }
    @Override protected void paint(GuiGraphicsExtractor graphics, int mx, int my) {
        UiPaint.text(graphics, text("id"), px + 24, py + 56, .78f, palette.muted, 1);
        UiPaint.text(graphics, text("formats"), px + 24, py + 116, .72f, palette.muted, 1);
        String hint = status;
        if (!busy && !id.isBlank() && !ToolVisualsClient.SOUNDS.availableId(id))
            hint = text(SoundIds.valid(SoundIds.normalize(id)) ? "duplicate" : "id_rule").getString();
        UiPaint.text(graphics, UiPaint.fit(graphics, Component.literal(hint), 672, .72f), px + 24, py + 143, .72f, palette.accentText, 1);
        if (ToolVisualsClient.SOUNDS.entries().isEmpty()) UiPaint.centered(graphics, text("empty").getString(), px + 24, py + 230, 672, 80, .9f, palette.muted, 1);
        UiPaint.centered(graphics, text("help").getString(), px + 24, py + 466, 672, 28, .72f, palette.muted, 1);
    }
}
