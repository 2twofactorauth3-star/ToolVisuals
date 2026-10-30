package ru.he3hauka.toolvisuals.client.social.ui;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;

import ru.he3hauka.toolvisuals.animation.PairEmote;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.social.PairTargets;
import ru.he3hauka.toolvisuals.client.social.SocialClient;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.EMOTE_SYNC;

public final class PairedEmoteScreen extends SocialScreen {
    private final UUID target;
    private List<VisualsButton> choices;
    public PairedEmoteScreen(UUID target) { super("title", 328); this.target = java.util.Objects.requireNonNull(target); }
    private Player target() {
        if (minecraft.level == null || minecraft.player == null) return null;
        var player = minecraft.level.getPlayerByUUID(target);
        return PairTargets.available(minecraft,player)?player:null;
    }
    @Override protected void build() {
        var list = new java.util.ArrayList<VisualsButton>();
        for (var emote : PairEmote.values()) list.add(button(24 + emote.id % 2 * 222, 100 + emote.id / 2 * 44, 210, tr(emote.key), () -> {
            if (target() == null || !SocialClient.available() || !ToolVisualsClient.CONFIG.flag(EMOTE_SYNC)) return;
            SocialClient.invite(target, emote); minecraft.gui.setScreen(null);
        }));
        choices = List.copyOf(list);
        button(164, 278, 152, tr("back"), this::onClose);
    }
    @Override protected void body(GuiGraphicsExtractor g, UiPalette palette, float opacity) {
        if (minecraft.player == null || minecraft.level == null || !ToolVisualsClient.CONFIG.active(ru.he3hauka.toolvisuals.client.module.VisualModule.ANIMATIONS)) { onClose(); return; }
        boolean ready = SocialClient.available() && ToolVisualsClient.CONFIG.flag(EMOTE_SYNC);
        boolean exists = target() != null;
        for (var button : choices) button.active = ready && exists;
        line(g, tr("with_player", SocialClient.name(target)).getString(), 54, .95f, palette.accentText, opacity);
        line(g, tr(!exists ? "target_unavailable" : ready ? "consent" : "server_required").getString(), 238, .75f, palette.muted, opacity);
    }
    @Override public void onClose() { minecraft.gui.setScreen(null); }
}
