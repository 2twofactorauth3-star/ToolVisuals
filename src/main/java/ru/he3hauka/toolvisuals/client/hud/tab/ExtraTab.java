package ru.he3hauka.toolvisuals.client.hud.tab;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.hud.tab.font.TabFonts;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.EXTRA_TAB;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ExtraTab {
    private static List<PlayerInfo> cached = List.of();
    private static Object connection;
    private static long rosterRevision=Long.MIN_VALUE,revision=-1,measuredRevision=Long.MIN_VALUE;
    private static Object measuredLanguage;
    private static net.minecraft.client.gui.Font measuredFont;
    private static int listedCount;
    private static Context context;
    private static TabViewport viewport;
    private static List<PlayerInfo> measured;
    private static int measuredWidth, measuredHeight, headerHeight, footerHeight;
    private static Component measuredHeader, measuredFooter;
    private static Objective measuredObjective;
    private static TabViewport measuredViewport;
    private static final class Context {
        public final PlayerTabOverlay overlay;
        public final int width, height;
        public final Component header, footer;
        public final Scoreboard scoreboard;
        public final Objective objective;
        private int headerHeight, footerHeight;
        Context(PlayerTabOverlay overlay, int width, int height, Component header, Component footer, Scoreboard scoreboard, Objective objective) {
            this.overlay = overlay; this.width = width; this.height = height; this.header = header; this.footer = footer;
            this.scoreboard = scoreboard; this.objective = objective;
        }
    }
    private ExtraTab() {}
    public static boolean enabled() { return ToolVisualsClient.CONFIG.active(EXTRA_TAB); }
    public static void tick(Minecraft client) {
        if (!canInteract(client)) viewport = null;
        if (client.player == null) { cached = List.of(); connection = null; measured = null; measuredViewport = null; }
    }
    private static boolean canInteract(Minecraft client) {
        return enabled() && client.player != null && client.level != null && client.gui.screen() == null
                && client.gui.overlay() == null && !client.gui.hud.isHidden() && client.options.keyPlayerList.isDown()
                && !NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.PLAYER_LIST)
                && !NoRender.hides(ru.he3hauka.toolvisuals.client.render.filter.RenderOption.ALL_HUD);
    }
    public static boolean showing() {
        var client = Minecraft.getInstance();
        return canInteract(client) && (!client.isLocalServer() || client.getConnection().getListedOnlinePlayers().size() > 1
                || client.level.getScoreboard().getDisplayObjective(net.minecraft.world.scores.DisplaySlot.LIST) != null);
    }
    public static TabViewport begin(PlayerTabOverlay overlay, int width, int height, Component header, Component footer,
                                    Scoreboard scoreboard, Objective objective, List<PlayerInfo> players) {
        var config = ToolVisualsClient.CONFIG;
        context = new Context(overlay, width, height, config.flag(TAB_HEADER) ? header : null,
                config.flag(TAB_FOOTER) ? footer : null, scoreboard, objective);
        long started=ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.start(ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.TAB);
        try{measure(players);}finally{ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.end(ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics.Section.TAB,started);}
        return viewport;
    }
    public static void end() { context = null; }
    public static net.minecraft.client.gui.Font font(net.minecraft.client.gui.Font original) {
        if(context==null||viewport==null)return original;
        double pixelsPerUnit=viewport.scale*Minecraft.getInstance().getWindow().getWidth()/(double)Math.max(1,context.width);
        return TabFonts.get(original,ru.he3hauka.toolvisuals.client.hud.tab.font.TabFontRaster.select(pixelsPerUnit));
    }
    public static List<PlayerInfo> players(Supplier<List<PlayerInfo>> original) {
        if (!enabled()) return original.get();
        var client = Minecraft.getInstance(); var config = ToolVisualsClient.CONFIG;
        int count = client.getConnection().getListedOnlinePlayers().size();
        long now=ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster.presentation();
        if (connection != client.getConnection() || rosterRevision != now || listedCount != count || revision != config.revision()) {
            cached = original.get(); connection = client.getConnection(); rosterRevision = now; listedCount = count; revision = config.revision();
        }
        return cached;
    }
    private static void measure(List<PlayerInfo> players) {
        var client = Minecraft.getInstance(); var config = ToolVisualsClient.CONFIG;
        var font = TabFonts.get(client.font);
        if (players == measured && measuredWidth == context.width && measuredHeight == context.height
                && measuredHeader == context.header && measuredFooter == context.footer && measuredObjective == context.objective
                && measuredFont == font && measuredRevision==ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster.presentation()&&measuredLanguage==net.minecraft.locale.Language.getInstance()) {
            viewport = measuredViewport; context.headerHeight = headerHeight; context.footerHeight = footerHeight;
            return;
        }
        int nameWidth = 0, scoreWidth = 0;
        for (var info : players) {
            nameWidth = Math.max(nameWidth, font.width(context.overlay.getNameForDisplay(info)));
            if (context.objective != null) {
                if (context.objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) scoreWidth = 90;
                else {
                    var score = context.scoreboard.getPlayerScoreInfo(ScoreHolder.fromGameProfile(info.getProfile()), context.objective);
                    if (score != null) scoreWidth = Math.max(scoreWidth,
                            font.width(score.formatValue(context.objective.numberFormatOrDefault(StyledFormat.PLAYER_LIST_DEFAULT))) + font.width(" "));
                }
            }
        }
        float preferredScale = config.number(TAB_SCALE);
        int textWidth = Math.max(1, (int)(context.width / preferredScale));
        context.headerHeight = textHeight(context.header, textWidth);
        context.footerHeight = textHeight(context.footer, textWidth);
        int columnWidth = nameWidth + scoreWidth + (client.isLocalServer() || client.getConnection().onlineMode() ? 9 : 0) + 13;
        viewport = new TabViewport(players.size(), context.width, context.height, columnWidth,
                context.headerHeight + context.footerHeight, preferredScale,Math.round(badgeScale()*26));
         
        context.headerHeight = textHeight(context.header, viewport.width);
        context.footerHeight = textHeight(context.footer, viewport.width);
        measuredRevision=ru.he3hauka.toolvisuals.client.optimization.hud.PlayerRoster.presentation();measuredLanguage=net.minecraft.locale.Language.getInstance();
        measuredFont = font;
        measured = players; measuredWidth = context.width; measuredHeight = context.height;
        measuredHeader = context.header; measuredFooter = context.footer; measuredObjective = context.objective;
        headerHeight = context.headerHeight; footerHeight = context.footerHeight; measuredViewport = viewport;
    }
    private static int textHeight(Component text, int width) {
        return text == null ? 0 : TabFonts.get(Minecraft.getInstance().font).split(text, Math.max(1, width - 50)).size() * 9 + 2;
    }
    public static void decorations(GuiGraphicsExtractor graphics) {
        if (context == null || viewport == null) return;
        var palette = new UiPalette(ToolVisualsClient.CONFIG.lightTheme() ? 1 : 0);
        Component hint = UiI18n.tr("toolvisuals.tab.count", viewport.total);
        float density=badgeScale();
        graphics.pose().pushMatrix();graphics.pose().scale(density,density);
        try {
            float height=22,textScale=.78f;
            float width=UiPaint.textWidth(graphics,hint.getString(),textScale)+24;
            float x=(graphics.guiWidth()/density-width)/2;
            float tableBottom=(16+context.headerHeight+viewport.rows*9+context.footerHeight)*viewport.scale/density;
            float y=Math.clamp(tableBottom+4,3,Math.max(3,graphics.guiHeight()/density-height-4));
            UiPaint.rounded(graphics,x,y,width,height,6,palette.hudPanel);
            UiPaint.centered(graphics,hint.getString(),x,y,width,height,textScale,palette.text,1);
        } finally { graphics.pose().popMatrix(); }
    }
    private static float badgeScale() {
        var window=Minecraft.getInstance().getWindow();
        return Math.clamp(window.getHeight()/720f,1,2)/window.getGuiScale();
    }
}
