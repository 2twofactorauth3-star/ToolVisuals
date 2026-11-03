package ru.he3hauka.toolvisuals.client.gui.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.armor.ui.ArmorSettingsScreen;
import ru.he3hauka.toolvisuals.client.auction.ui.AuctionScreen;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.config.color.ColorSettings;
import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.config.ui.ConfigScreen;
import ru.he3hauka.toolvisuals.client.crosshair.CrosshairPainter;
import ru.he3hauka.toolvisuals.client.crosshair.ui.CrosshairEditorScreen;
import ru.he3hauka.toolvisuals.client.friends.ui.FriendsScreen;
import ru.he3hauka.toolvisuals.client.gui.animation.Motion;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.layout.UiViewport;
import ru.he3hauka.toolvisuals.client.gui.input.GuiMovement;
import ru.he3hauka.toolvisuals.client.gui.input.MovementScreen;
import ru.he3hauka.toolvisuals.client.gui.render.UiFont;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.render.UiSprites;
import ru.he3hauka.toolvisuals.client.gui.render.UiTextRasterizer;
import ru.he3hauka.toolvisuals.client.gui.surface.UiPalette;
import ru.he3hauka.toolvisuals.client.hud.layout.HudEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;
import ru.he3hauka.toolvisuals.client.module.ModuleCategory;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.pvp.ui.PvPCommandsScreen;
import ru.he3hauka.toolvisuals.client.render.filter.RenderCategory;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;
import ru.he3hauka.toolvisuals.client.search.ModuleSearch;
import ru.he3hauka.toolvisuals.client.waypoint.ui.WaypointsScreen;

import static ru.he3hauka.toolvisuals.client.gui.module.VisualsButton.Kind.*;

public class VisualsScreen extends Screen implements MovementScreen {
    private long serverPolicyRevision = Long.MIN_VALUE;
    public boolean settingsAvailable() { return selectedModule == null || config.available(selectedModule); }
    private void refreshServerPolicy() {
        long revision = config.serverPolicy.revision();
        if (serverPolicyRevision == revision) return;
        serverPolicyRevision = revision;
        if (!settingsAvailable()) selectedModule = null;
        rebuildWidgets();
    }
    private static final int MODULE_PITCH = 70, OPTION_PITCH = 36, SETTING_PITCH = 58;
    private static final java.net.URI FEEDBACK_URL=java.net.URI.create("https://t.me/toolvisuals");
    private static ModuleCategory lastCategory = ModuleCategory.WORLD;
    private final ClientConfig config = ToolVisualsClient.CONFIG;
    private final List<Slot> chrome = new ArrayList<>(), modules = new ArrayList<>(), paneControls = new ArrayList<>();
    private final List<AbstractWidget> rows = new ArrayList<>();
    private final List<VisualsButton> popup = new ArrayList<>();
    private final Map<VisualsButton, VisualModule> moduleIds = new LinkedHashMap<>();
    private final Motion opening = new Motion(0), paneOpening = new Motion(0), moduleScroll = new Motion(0), scroll = new Motion(0);
    private final Motion theme = new Motion(config.lightTheme() ? 1 : 0);
    private UiViewport viewport;
    private UiPalette palette = new UiPalette(0);
    private ModuleCategory category = lastCategory;
    private VisualModule selectedModule;
    private RenderCategory filter;
    private List<RenderOption> filtered = List.of();
    private EditBox search;
    private EditBox moduleSearch;
    private String moduleQuery="";
    private EditBox protectedNameField;
    private VisualsButton hudEditorButton;
    private VisualsButton feedbackButton;
    private String query = "";
    private VisualSetting expandedChoice;
    private boolean closing, compact, draggingScroll, draggingModules;
    private int px, py, pw, ph, moduleWidth, columns, paneX, paneWidth, paneTop, paneBottom;
    private int listX, listY, listWidth, listBottom, contentHeight;
    private int popupX, popupY, popupWidth, popupHeight, popupFirst, popupRows;
    private float elapsed, opacity, scrollTarget, moduleScrollTarget, scrollbarGrab;
    private long lastFrame, crosshairPreviewStart;

    private static final class Slot {
        public final VisualsButton button;
        public final int x, y;
        Slot(VisualsButton button, int x, int y) { this.button = button; this.x = x; this.y = y; }
    }
    public VisualsScreen() { this(null); }
    public VisualsScreen(VisualModule module) {
        super(Component.literal("ToolVisuals")); selectedModule = module;
        if (module != null) category = lastCategory = module.category;
    }
    private static Component tr(String key, Object... args) { return UiI18n.tr("toolvisuals." + key, args); }
    private boolean noRender() { return selectedModule == VisualModule.NO_RENDER; }
    private boolean crosshair() { return selectedModule == VisualModule.CROSSHAIR; }
    private boolean denseFilters() { return noRender() && ph < 380; }
    private int searchOffset() { return denseFilters() ? 34 : 62; }

    @Override protected void init() {
        serverPolicyRevision = config.serverPolicy.revision();
        chrome.clear(); modules.clear(); paneControls.clear(); rows.clear(); popup.clear(); moduleIds.clear(); expandedChoice = null;
        moduleSearch=null;feedbackButton=null;
        viewport = new UiViewport(width, height, minecraft.getWindow().getGuiScale());
        var layout = new UiMenuLayout(viewport.width, Math.max(240, viewport.height - 46), selectedModule != null);
        px = layout.x; py = layout.y; pw = layout.width; ph = layout.height;
        moduleWidth = layout.moduleWidth; paneWidth = layout.paneWidth; compact = layout.compact;
        columns = moduleWidth >= 420 ? 2 : 1;
        paneX = compact ? px + 18 : px + moduleWidth + 20;
        paneTop = py + 62; paneBottom = py + ph - 18;
        add(chrome, pw - 104, 12, 28, 26, Component.literal(config.language().badge), CHIP, null,
                tr(config.language() == UiLanguage.ENGLISH ? "ui.language_to_ru" : "ui.language_to_en"), () -> {
                    config.setLanguage(config.language().next()); UiI18n.use(config.language()); rebuildWidgets();
                }, null);
        add(chrome, pw - 70, 12, 26, 26, tr("ui.theme"), ICON, UiPaint.Icon.SUN, tr("ui.theme"),
                () -> config.setLightTheme(!config.lightTheme()), null);
        add(chrome, pw - 38, 12, 26, 26, tr("ui.close"), ICON, UiPaint.Icon.CLOSE, tr("ui.close"), this::closeMenu, null);
        hudEditorButton = config.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.HUD_EDITOR)
                ? add(chrome, (pw - 208) / 2, ph + 10, 208, 34, tr("hud.edit"), CHIP, UiPaint.Icon.HUD,
                tr("hud.edit_hint"), () -> minecraft.gui.setScreen(new HudEditorScreen(this)), null) : null;
        if (!(compact && selectedModule != null)) {
            var categories = Arrays.stream(ModuleCategory.values()).filter(next -> next != ModuleCategory.FRIENDS
                    || config.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.FRIENDS)).toArray(ModuleCategory[]::new);
            int available = moduleWidth - 32;
            int[] widths = Arrays.stream(categories).mapToInt(tab -> (int)Math.ceil(UiTextRasterizer.metrics(13).width(tab.label().getString())) + 32).toArray();
            boolean stacked = Arrays.stream(widths).sum() > available;
            if (stacked) widths = Arrays.stream(categories).mapToInt(tab -> (int)Math.ceil(UiTextRasterizer.metrics(12).width(tab.label().getString())) + 14).toArray();
            int minimum = Arrays.stream(widths).sum(), remaining = available - minimum, tabX = 16;
            for (int i = 0; i < categories.length; i++) {
                ModuleCategory next = categories[i];
                int tabWidth = widths[i] + (i + 1) * remaining / categories.length - i * remaining / categories.length;
                var tab = add(chrome, tabX, 58, tabWidth - 2, 30, next.label(), TAB,
                        categoryIcon(next), next.description(), () -> selectCategory(next), () -> moduleQuery.isBlank()&&category == next);
                tab.stackedTab = stacked;
                tabX += tabWidth;
            }
            add(chrome, moduleWidth - 43, ph - 35, 26, 24, tr("keybinds"), ICON, UiPaint.Icon.KEYBOARD,
                    tr("keybinds"), () -> minecraft.gui.setScreen(new KeyBindsScreen(this,minecraft.options)), null);
            moduleSearch=addWidget(new EditBox(UiFont.get(),0,0,moduleWidth-125,28,tr("module_search.hint")) {
                @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
                    g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(getHeight()));super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
                }
            });
            moduleSearch.setBordered(false);moduleSearch.setTextShadow(false);moduleSearch.setMaxLength(80);moduleSearch.setValue(moduleQuery);
            moduleSearch.setResponder(text->{moduleQuery=text;moduleScrollTarget=0;moduleScroll.snap(0);modules.forEach(slot->removeWidget(slot.button));modules.clear();moduleIds.clear();buildModules();positionWidgets();});
            add(chrome,moduleWidth-75,ph-35,24,24,tr("ui.clear_search"),ICON,UiPaint.Icon.CLOSE,tr("ui.clear_search"),()->moduleSearch.setValue(""),null);
            feedbackButton=addWidget(new VisualsButton(0,0,200,32,Component.literal("t.me/toolvisuals"),CHIP,UiPaint.Icon.ARROW,
                    tr("module_search.contact"),()->com.mojang.blaze3d.Blaze3D.openUri(FEEDBACK_URL),null));
            buildModules();
        }
        if (selectedModule != null) buildPane(); else search = null;
        positionWidgets(); setFocused(null); lastFrame = System.nanoTime();
    }
    private VisualsButton add(List<Slot> group, int x, int y, int width, int height, Component label,
                              VisualsButton.Kind kind, UiPaint.Icon icon, Component hint, Runnable action, BooleanSupplier selected) {
        var button = addWidget(new VisualsButton(x, y, width, height, label, kind, icon, hint, action, selected));
        group.add(new Slot(button, x, y)); return button;
    }
    private static UiPaint.Icon categoryIcon(ModuleCategory category) {
        return switch (category) {
            case WORLD -> UiPaint.Icon.WORLD;
            case PLAYER -> UiPaint.Icon.PLAYER;
            case RENDER -> UiPaint.Icon.RENDER;
            case HUD -> UiPaint.Icon.HUD;
            case CONFIGS -> UiPaint.Icon.SETTINGS;
            case FRIENDS -> UiPaint.Icon.PLAYER;
            case FAVORITES -> UiPaint.Icon.STAR;
        };
    }
    private void selectCategory(ModuleCategory next) {
        if (next == ModuleCategory.CONFIGS) { minecraft.gui.setScreen(new ConfigScreen(this)); return; }
        if (next == ModuleCategory.FRIENDS) {
            if (config.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.FRIENDS)) minecraft.gui.setScreen(new FriendsScreen(this));
            return;
        }
        category = lastCategory = next; selectedModule = null; filter = null;
        moduleQuery="";
        moduleScrollTarget = 0; moduleScroll.snap(0); rebuildWidgets();
    }
    private void buildModules() {
        int cardWidth = (moduleWidth - 36 - (columns - 1) * 10) / columns, index = 0;
        for (VisualModule module : moduleQuery.isBlank()?List.of(VisualModule.values()):ModuleSearch.find(moduleQuery)) {
            if (!config.available(module)) continue;
            if (moduleQuery.isBlank()&&(category == ModuleCategory.FAVORITES ? !config.favorite(module) : module.category != category)) continue;
            int x = 18 + index % columns * (cardWidth + 10), y = 104 + index / columns * MODULE_PITCH;
            var button = add(modules, x, y, cardWidth - 32, 62, Component.literal(module.title), MODULE, null,
                    module.description(), () -> toggleModule(module), () -> config.active(module));
            moduleIds.put(button, module);
            button.muteClick = module == VisualModule.SOUND_CUSTOMIZER;
            add(modules, x + cardWidth - 30, y + 5, 25, 25, tr("module.settings"), ICON, UiPaint.Icon.SETTINGS,
                    tr("module.settings"), () -> openSettings(module), () -> selectedModule == module);
            add(modules, x + cardWidth - 30, y + 33, 25, 25, tr("favorites.toggle"), ICON, UiPaint.Icon.STAR,
                    tr(config.favorite(module) ? "favorites.remove" : "favorites.add"), () -> {
                        config.toggleFavorite(module); rebuildWidgets();
                    }, () -> config.favorite(module));
            index++;
        }
        moduleScrollTarget = Math.clamp(moduleScrollTarget, 0, maxModuleScroll());
        if (moduleScroll.value() > maxModuleScroll()) moduleScroll.snap(moduleScrollTarget);
    }
    private void openSettings(VisualModule module) {
        if (!config.available(module)) return;
        if(module==VisualModule.CUSTOM_HAND){minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.hand.ui.HandSettingsScreen(this));return;}
        if (module == VisualModule.DAMAGE_VIGNETTE) {
            minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.render.vignette.ui.DamageVignetteScreen(this));return;
        }
        if (module == VisualModule.ARMOR_HUD || module == VisualModule.ARMOR_COLOR) {
            minecraft.gui.setScreen(new ArmorSettingsScreen(this, module == VisualModule.ARMOR_COLOR)); return;
        }
        if (module == VisualModule.AUCTION_HELPER) { minecraft.gui.setScreen(new AuctionScreen(this)); return; }
        if (selectedModule == module) { closeSettings(); return; }
        selectedModule = module; scroll.snap(0); scrollTarget = 0; paneOpening.snap(0); query = ""; filter = null; rebuildWidgets();
    }
    private void toggleModule(VisualModule module) {
        boolean enabled = !config.active(module), sound = config.active(VisualModule.SOUND_CUSTOMIZER);
        if(sound)ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.module(enabled);
        config.setActive(module, enabled);
        if(!sound&&module==VisualModule.SOUND_CUSTOMIZER&&enabled)ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.module(true);
    }
    private void closeSettings() { selectedModule = null; draggingScroll = false; scrollTarget = 0; scroll.snap(0); rebuildWidgets(); }
    private void buildPane() {
        add(paneControls, paneWidth - 30, 0, 26, 26, tr("ui.back"), ICON, UiPaint.Icon.BACK,
                tr("ui.back"), this::closeSettings, null);
        var masterToggle=add(paneControls, paneWidth - 72, 0, 34, 26, Component.literal(selectedModule.title), SWITCH, null,
                tr("master_hint"), () -> toggleModule(selectedModule), () -> config.active(selectedModule));
        masterToggle.moduleSound=true;masterToggle.muteClick=selectedModule==VisualModule.SOUND_CUSTOMIZER;
        if (noRender()) {
            search = addWidget(new EditBox(UiFont.get(), 0, 0, paneWidth - 68, 30, tr("search")) {
                @Override public void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
                    g.pose().pushMatrix(); g.pose().translate(0, UiFont.centeredTextOffset(getHeight()));
                    super.extractWidgetRenderState(g, mx, my, partial); g.pose().popMatrix();
                }
            });
            search.setBordered(false); search.setTextShadow(false); search.setMaxLength(100); search.setValue(query);
            search.setResponder(text -> { query = text; refreshRows(); });
            add(paneControls, paneWidth - 30, searchOffset() + 3, 22, 24, tr("ui.clear_search"), ICON, UiPaint.Icon.CLOSE,
                    tr("ui.clear_search"), () -> search.setValue(""), null);
            int groupColumns = denseFilters() ? 6 : 3, groupWidth = (paneWidth - (groupColumns - 1) * 4) / groupColumns;
            for (int i = 0; i <= RenderCategory.values().length; i++) {
                RenderCategory next = i == 0 ? null : RenderCategory.values()[i - 1];
                var label = next == null ? tr("ui.all") : next.label();
                var icon = !denseFilters() || next == null ? null : switch (next) {
                    case WORLD -> UiPaint.Icon.WORLD;
                    case ENTITIES -> UiPaint.Icon.PLAYER;
                    case EFFECTS -> UiPaint.Icon.RENDER;
                    case CAMERA -> UiPaint.Icon.CAMERA;
                    case HUD -> UiPaint.Icon.HUD;
                };
                add(paneControls, i % groupColumns * (groupWidth + 4), (denseFilters() ? 72 : 104) + i / groupColumns * 29, groupWidth, 26,
                        label, icon == null ? CHIP : ICON, icon, label,
                        () -> { filter = next; refreshRows(); }, () -> filter == next);
            }
            int actionWidth = (paneWidth - 8) / 3;
            add(paneControls, 0, paneBottom - paneTop - 26, actionWidth, 26, tr("ui.hide"), GHOST, null,
                    tr("ui.bulk_hide"), () -> config.setSelected(filtered, true), null);
            add(paneControls, actionWidth + 4, paneBottom - paneTop - 26, actionWidth, 26, tr("ui.show"), GHOST, null,
                    tr("ui.bulk_show"), () -> config.setSelected(filtered, false), null);
            add(paneControls, (actionWidth + 4) * 2, paneBottom - paneTop - 26, actionWidth, 26, tr("reset"), GHOST, null,
                    tr("ui.reset_hint"), () -> config.reset(selectedModule), null);
        } else {
            search = null;
            add(paneControls, 0, paneBottom - paneTop - 26, paneWidth, 26, tr("reset"), GHOST, UiPaint.Icon.RESET,
                    Component.empty(), () -> { config.reset(selectedModule); refreshRows(); }, null);
        }
        refreshRows();
    }
    private void refreshRows() {
        if (protectedNameField != null && protectedNameField.isFocused()) setFocused(null);
        protectedNameField = null;
        rows.forEach(this::removeWidget); rows.clear(); closeChoice();
        listX = paneX; listWidth = paneWidth; listY = paneTop + (noRender() ? denseFilters() ? 106 : 172 : crosshair() ? ph < 380 ? 110 : 180 : 68); listBottom = paneBottom - 42;
        if (noRender()) {
            String needle = query.strip().toLowerCase(Locale.ROOT);
            filtered = Arrays.stream(RenderOption.values()).filter(config::available).filter(option -> filter == null || option.category == filter)
                    .filter(option -> needle.isEmpty() || option.id.contains(needle)
                            || option.label().getString().toLowerCase(Locale.ROOT).contains(needle)
                            || option.description().getString().toLowerCase(Locale.ROOT).contains(needle)).toList();
            for (RenderOption option : filtered) rows.add(addWidget(new VisualsButton(0, 0, paneWidth - 8, 32,
                    option.label(), OPTION, null, option.description(), () -> config.toggle(option), () -> config.selected(option))));
            contentHeight = rows.size() * OPTION_PITCH;
        } else {
            if(selectedModule==VisualModule.SOUND_CUSTOMIZER)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.sound_library.open"),CHIP,UiPaint.Icon.SETTINGS,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.sound.ui.SoundLibraryScreen(this)),null)));
            if(selectedModule==VisualModule.GPS)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.gps.manage"),CHIP,UiPaint.Icon.SETTINGS,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.navigation.gps.ui.GpsScreen(this)),null)));
            if(selectedModule==VisualModule.MINI_GAMES)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.minigames.open"),CHIP,UiPaint.Icon.WORLD,Component.empty(),()->ru.he3hauka.toolvisuals.client.minigame.MiniGamesClient.open(this),null)));
            if((selectedModule==VisualModule.CAPES||selectedModule==VisualModule.COSMETICS)&&config.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.OUTFITS))rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.outfits.title"),CHIP,UiPaint.Icon.STAR,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.cosmetic.outfit.ui.OutfitScreen(this)),null)));
            if(selectedModule==VisualModule.CLIENT_LIST)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.client_list.open"),CHIP,UiPaint.Icon.PLAYER,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.social.presence.ui.ClientBrowserScreen(this)),null)));
            if(selectedModule==VisualModule.SESSION_STATS)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.session.reset"),GHOST,UiPaint.Icon.RESET,Component.empty(),ru.he3hauka.toolvisuals.client.session.SessionStats::reset,null)));
            if(selectedModule==VisualModule.MUSIC_HUD)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.music.open"),CHIP,UiPaint.Icon.MUSIC,Component.empty(),()->ru.he3hauka.toolvisuals.client.music.MusicClient.open(this),null)));
            if(selectedModule==VisualModule.KEYBINDS)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.keybinds.open"),CHIP,UiPaint.Icon.KEYBOARD,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.keybinds.ui.KeybindsScreen(this)),null)));
            if(selectedModule==VisualModule.REPLAY)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.replay.open"),CHIP,UiPaint.Icon.CAMERA,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.replay.ui.ReplayLibraryScreen(this)),null)));
            if(selectedModule==VisualModule.VOICE_CHAT)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.voice.open"),CHIP,UiPaint.Icon.SETTINGS,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.voice.ui.VoiceChatScreen(this)),null)));
            if(selectedModule==VisualModule.COSMETICS)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.cosmetics.open"),CHIP,UiPaint.Icon.PLAYER,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.cosmetic.accessory.ui.CosmeticsScreen(this)),null)));
            if(selectedModule==VisualModule.CAPES)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.capes.open"),CHIP,UiPaint.Icon.RENDER,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.cape.ui.CapeEditorScreen(this)),null)));
            if(crosshair())rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,UiI18n.tr("toolvisuals.editor.draw_open"),CHIP,UiPaint.Icon.RENDER,Component.empty(),()->minecraft.gui.setScreen(new CrosshairEditorScreen(this)),null)));
            if(selectedModule!=null&&selectedModule.category==ModuleCategory.HUD&&config.available(ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature.HUD_EDITOR))rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("utility.hud_preview"),CHIP,UiPaint.Icon.HUD,Component.empty(),()->minecraft.gui.setScreen(new HudEditorScreen(this)),null)));
            if (selectedModule == VisualModule.NAME_PROTECT) {
                protectedNameField = addWidget(new ProtectedNameField()); rows.add(protectedNameField);
            }
            if(selectedModule==VisualModule.CHAT_TABS||selectedModule==VisualModule.STREAMER_MODE)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr(selectedModule==VisualModule.CHAT_TABS?"utility.chat":"utility.privacy_names"),CHIP,UiPaint.Icon.SETTINGS,Component.empty(),()->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.utility.chat.ui.ChatToolsScreen(this,selectedModule==VisualModule.STREAMER_MODE)),null)));
            if(selectedModule==VisualModule.ANIMATIONS) {
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("emotes.open"),CHIP,UiPaint.Icon.PLAYER,Component.empty(),()->{config.setActive(VisualModule.ANIMATIONS,true);minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.animation.ui.EmoteWheelScreen(this));},null)));
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("emotes.solo_key",ToolVisualsClient.emoteKey.getTranslatedKeyMessage()),CHIP,UiPaint.Icon.KEYBOARD,Component.empty(),()->minecraft.gui.setScreen(new KeyBindsScreen(this,minecraft.options)),null)));
                var pairKey=addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("emotes.pair_key",ToolVisualsClient.pairedEmoteKey.getTranslatedKeyMessage()),CHIP,UiPaint.Icon.KEYBOARD,Component.empty(),()->minecraft.gui.setScreen(new KeyBindsScreen(this,minecraft.options)),null));
                pairKey.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tr("emotes.pair_key_hint")));rows.add(pairKey);
            }
            if(selectedModule==VisualModule.FRIENDS_LIST)rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("friends.list_profiles"),CHIP,UiPaint.Icon.PLAYER,tr("friends.list_sync_hint"),()->minecraft.gui.setScreen(new FriendsScreen(this)),null)));
            for (VisualSetting setting : VisualSetting.values()) {
                if (setting.module != selectedModule || !config.available(setting)) continue;
                if (selectedModule == VisualModule.DROP_PROTECT && !ru.he3hauka.toolvisuals.client.items.dropprotect.DropCategory.visible(config, setting)) continue;
                if(setting==VisualSetting.ZOOM_FACTOR&&config.value(VisualSetting.ZOOM_RANGE_MODE)!=2
                        ||setting==VisualSetting.ZOOM_DISTANCE&&config.value(VisualSetting.ZOOM_RANGE_MODE)!=1)continue;
                if (crosshair() && !CrosshairPainter.visibleSetting(setting)) continue;
                if (ColorSettings.contains(setting)) rows.add(addWidget(new ColorButton(setting)));
                else if (setting.kind == VisualSetting.Kind.NUMBER) rows.add(addWidget(new SettingSlider(setting)));
                else if (setting.kind == VisualSetting.Kind.BOOLEAN) rows.add(addWidget(new VisualsButton(0, 0, paneWidth - 8, 50,
                        setting.label(), OPTION, null, Component.empty(), () -> {
                            config.setValue(setting, config.flag(setting) ? 0 : 1);
                            if (crosshair()) refreshCrosshairRows();
                        }, () -> config.flag(setting))));
                else rows.add(addWidget(new ChoiceButton(setting)));
            }
            if (selectedModule == VisualModule.FAKE_PLAYER) {
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("fake_player.spawn"),CHIP,UiPaint.Icon.PLUS,Component.empty(),
                        () -> { config.setActive(VisualModule.FAKE_PLAYER,true);ru.he3hauka.toolvisuals.client.player.FakePlayer.spawn(minecraft); },null)));
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("fake_player.remove"),GHOST,UiPaint.Icon.CLOSE,Component.empty(),
                        ru.he3hauka.toolvisuals.client.player.FakePlayer::remove,null)));
            }
            if (selectedModule == VisualModule.PVP_SAVE)
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("pvp_commands.open",config.pvpBlockedCommands().size()),CHIP,UiPaint.Icon.SETTINGS,Component.empty(),
                        () -> minecraft.gui.setScreen(new PvPCommandsScreen(this)),null)));
            if (selectedModule == VisualModule.WAYPOINTS)
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("waypoints.manage"),CHIP,UiPaint.Icon.WORLD,Component.empty(),
                        () -> minecraft.gui.setScreen(new WaypointsScreen(this)),null)));
            if(selectedModule==VisualModule.TOOL_PRESENTS) {
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("presents.recalculate"),CHIP,UiPaint.Icon.WORLD,tr("presents.route_hint"),
                        ru.he3hauka.toolvisuals.client.presents.ToolPresents::recalculate,null)));
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("presents.reset"),GHOST,UiPaint.Icon.RESET,Component.empty(),
                        () -> minecraft.gui.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(confirmed->{
                            if(confirmed)ru.he3hauka.toolvisuals.client.presents.ToolPresents.reset();
                            minecraft.gui.setScreen(this);
                        },tr("presents.reset"),tr("presents.reset_confirm"))),null)));
            }
            if(selectedModule==VisualModule.DEATH_ASCENSION)
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("death.preview"),CHIP,UiPaint.Icon.RENDER,Component.empty(),
                        ()->{config.setActive(VisualModule.DEATH_ASCENSION,true);ru.he3hauka.toolvisuals.client.animation.death.DeathAscension.preview();},null)));
            if (selectedModule == VisualModule.TOTEM_POP_COUNTER)
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("totem.clear"),GHOST,UiPaint.Icon.RESET,Component.empty(),
                        ru.he3hauka.toolvisuals.client.pvp.TotemCounter::clear,null)));
            if (selectedModule == VisualModule.SOUND_CUSTOMIZER)
                for(var channel:ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.Channel.values()) {
                    var preview=new VisualsButton(0,0,paneWidth-8,44,tr("sounds.preview_"+channel.name().toLowerCase(Locale.ROOT)),CHIP,UiPaint.Icon.RENDER,Component.empty(),
                            () -> ru.he3hauka.toolvisuals.client.sound.SoundCustomizer.play(channel,true,1),null);
                    preview.muteClick=true;rows.add(addWidget(preview));
                }
            if (selectedModule == VisualModule.CHANGE_HAND || selectedModule == VisualModule.ELYTRA_SWAP || selectedModule == VisualModule.FAKE_PLAYER || selectedModule == VisualModule.TAPE_MOUSE
                    || selectedModule == VisualModule.FREE_LOOK && config.value(VisualSetting.FREE_LOOK_MODE) != 0) {
                var key = selectedModule == VisualModule.CHANGE_HAND ? ToolVisualsClient.changeHandKey
                        : selectedModule == VisualModule.ELYTRA_SWAP ? ToolVisualsClient.elytraSwapKey
                        : selectedModule == VisualModule.TAPE_MOUSE ? ToolVisualsClient.tapeMouseKey
                        : selectedModule == VisualModule.FREE_LOOK ? ToolVisualsClient.freeLookKey : ToolVisualsClient.fakePlayerKey;
                rows.add(addWidget(new VisualsButton(0,0,paneWidth-8,44,tr("action.key",key.getTranslatedKeyMessage()),CHIP,UiPaint.Icon.KEYBOARD,Component.empty(),
                        () -> minecraft.gui.setScreen(new KeyBindsScreen(this,minecraft.options)),null)));
            }
            contentHeight = rows.size() * SETTING_PITCH;
        }
        scrollTarget = 0; scroll.snap(0); positionWidgets();
    }
    private void refreshCrosshairRows() {
        float old = scrollTarget;
        refreshRows(); scrollTarget = Math.clamp(old, 0, maxScroll()); scroll.snap(scrollTarget); positionWidgets();
    }
    private void positionWidgets() {
        for (Slot slot : chrome) place(slot, px, py, true);
        if (hudEditorButton != null) hudEditorButton.active = !closing && minecraft.player != null;
        for (Slot slot : modules) {
            place(slot, px, py - Math.round(moduleScroll.value()), true);
            slot.button.visible = slot.button.getY() < py + ph - 48 && slot.button.getY() + slot.button.getHeight() > py + 102;
        }
        for (Slot slot : paneControls) place(slot, paneX, paneTop, selectedModule != null);
        int pitch = noRender() ? OPTION_PITCH : SETTING_PITCH;
        for (int i = 0; i < rows.size(); i++) {
            var row = rows.get(i); row.setX(listX + (row == protectedNameField ? 8 : 0)); row.setY(listY + i * pitch - Math.round(scroll.value()));
            row.visible = selectedModule != null && row.getY() < listBottom && row.getY() + row.getHeight() > listY; row.active = !closing;
        }
        if (search != null) { search.setX(paneX + 30); search.setY(paneTop + searchOffset()); }
        if(moduleSearch!=null){moduleSearch.setX(px+45);moduleSearch.setY(py+ph-37);moduleSearch.active=!closing;}
        if(feedbackButton!=null){
            feedbackButton.visible=moduleSearch!=null&&!moduleQuery.isBlank()&&moduleIds.isEmpty();
            feedbackButton.active=!closing;
            int cardWidth=Math.min(480,moduleWidth-48),cardHeight=Math.min(176,ph-168);
            feedbackButton.setWidth(Math.min(216,cardWidth-32));
            feedbackButton.setHeight(cardHeight<116?26:32);
            feedbackButton.setX(px+(moduleWidth-cardWidth)/2+16);feedbackButton.setY(py+112+cardHeight-(cardHeight<116?10:14)-feedbackButton.getHeight());
        }
    }
    private void place(Slot slot, int x, int y, boolean visible) {
        slot.button.setX(x + slot.x); slot.button.setY(y + slot.y); slot.button.visible = visible; slot.button.active = !closing;
    }
    private float maxModuleScroll() { return Math.max(0, ((moduleIds.size() + columns - 1) / columns) * MODULE_PITCH - 8 - (ph - 152)); }
    private float maxScroll() { return Math.max(0, contentHeight - 8 - (listBottom - listY)); }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float partial) {
        if (minecraft.level == null) extractPanorama(g, partial);
        g.fill(0, 0, width, height, UiPaint.alpha(0x48060910, opening.value()));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
        refreshServerPolicy();
        if (width != g.guiWidth() || height != g.guiHeight()) { resize(g.guiWidth(), g.guiHeight()); return; }
        long now = System.nanoTime(); elapsed = Math.clamp((now - lastFrame) / 1_000_000_000f, 0, 1); lastFrame = now;
        opacity = opening.step(closing ? 0 : 1, elapsed, 20);
        if (closing && opacity < .01f) { minecraft.gui.setScreen(null); return; }
        paneOpening.step(1, elapsed, 20); moduleScroll.step(moduleScrollTarget, elapsed, 22); scroll.step(scrollTarget, elapsed, 22);
        palette = new UiPalette(theme.step(config.lightTheme() ? 1 : 0, elapsed, 16)); positionWidgets();
        var pointer = adjusted(new MouseButtonEvent(mx, my, new MouseButtonInfo(0, 0))); mx = (int)pointer.x(); my = (int)pointer.y();
        g.pose().pushMatrix(); g.pose().scale(viewport.scale, viewport.scale); g.pose().translate(0, (1 - opacity) * 6);
        UiPaint.shadow(g, px, py, pw, ph, 12, opacity);
        UiPaint.rounded(g, px - 1, py - 1, pw + 2, ph + 2, 13, UiPaint.alpha(palette.border, opacity));
        UiPaint.rounded(g, px, py, pw, ph, 12, UiPaint.alpha(palette.panel, opacity));
        if(selectedModule!=null&&!compact)UiPaint.rounded(g,paneX-14,py+56,paneWidth+28,ph-68,9,UiPaint.alpha(palette.inset,opacity));
        if(moduleSearch!=null){
            if(moduleSearch.isFocused())UiPaint.surface(g,px+18,py+ph-37,moduleWidth-66,28,6,palette.field,palette.border,opacity);
            else UiPaint.rounded(g,px+18,py+ph-37,moduleWidth-66,28,6,UiPaint.alpha(palette.field,opacity));
        }
        g.fill(px + 18, py + 49, px + pw - 18, py + 50, UiPaint.alpha(palette.border, opacity * .55f));
        UiPaint.icon(g, UiPaint.Icon.RENDER, px + 19, py + 17, 16, palette.accentText, opacity);
        UiPaint.baseline(g, "ToolVisuals", px + 45, UiPaint.textBaseline(g, py + 12, 26, 1.05f), 1.05f, palette.text, opacity);
        SettingsSaveIndicator.draw(g,config,px+158,py+12,Math.clamp(pw-365,48,150),palette,opacity);
        if (pw >= 420 && ToolVisualsClient.openMenuKey != null) {
            String key = ToolVisualsClient.openMenuKey.matches(ToolVisualsClient.openMenuKey.getDefaultKey())
                    ? tr("ui.menu_key").getString() : ToolVisualsClient.openMenuKey.getTranslatedKeyMessage().getString();
            float w = Math.min(110, UiPaint.textWidth(g, key, .8f) + 18);
            UiPaint.rounded(g, px + pw - 118 - w, py + 14, w, 23, 6, UiPaint.alpha(palette.raised, opacity));
            UiPaint.centered(g, UiPaint.fit(g, Component.literal(key), w - 12, .8f), px + pw - 118 - w, py + 14, w, 23, .8f, palette.muted, opacity);
        }
        drawButtons(g, chrome.stream().filter(slot -> slot.button.kind != TAB).toList(), mx, my, opacity);
        if (!(compact && selectedModule != null)) drawModules(g, mx, my);
        if (selectedModule != null) drawPane(g, mx, my, opacity * paneOpening.value());
        if (expandedChoice != null) drawPopup(g, mx, my); else drawHint(g, mx, my);
        g.pose().popMatrix();
    }
    private void drawModules(GuiGraphicsExtractor g, int mx, int my) {
        UiPaint.rounded(g,px+16,py+56,moduleWidth-32,34,8,UiPaint.alpha(palette.inset,opacity));
        drawButtons(g, chrome.stream().filter(slot -> slot.button.kind == TAB).toList(), mx, my, opacity);
        g.enableScissor(px + 16, py + 102, px + moduleWidth - 12, py + ph - 48);
        for (var entry : moduleIds.entrySet()) {
            var b = entry.getKey(); boolean chosen = entry.getValue() == selectedModule;
            boolean hovered=mx>=b.getX()&&mx<b.getX()+b.getWidth()+32&&my>=b.getY()&&my<b.getY()+62;
            int fill=chosen?palette.selected:UiPalette.mix(palette.raised,palette.hover,hovered?.45f:0);
            UiPaint.rounded(g,b.getX(),b.getY(),b.getWidth()+32,62,8,UiPaint.alpha(fill,opacity));
        }
        int hitY = my >= py + 102 && my < py + ph - 48 && expandedChoice == null ? my : -1000;
        drawButtons(g, modules, expandedChoice == null ? mx : -1000, hitY, opacity); g.disableScissor();
        if(moduleIds.isEmpty()){
            if(moduleQuery.isBlank())UiPaint.centered(g,tr("favorites.empty").getString(),px+24,py+124,moduleWidth-48,48,.85f,palette.muted,opacity);
            else drawEmptySearch(g,mx,my);
        }
        drawScrollbar(g, px + moduleWidth - 11, py + 104, ph - 152, moduleScroll.value(), maxModuleScroll(), opacity);
        g.fill(px + 18, py + ph - 43, px + moduleWidth - 18, py + ph - 42, UiPaint.alpha(palette.border, opacity * .45f));
        UiPaint.icon(g,UiPaint.Icon.SEARCH,px+25,py+ph-29,12,palette.muted,opacity);
        if(moduleQuery.isEmpty()&&!moduleSearch.isFocused())UiPaint.baseline(g,UiPaint.fit(g,tr("module_search.hint"),moduleSearch.getWidth(),.8f),moduleSearch.getX(),UiPaint.textBaseline(g,moduleSearch.getY(),28,.8f),.8f,palette.faint,opacity);
        moduleSearch.setTextColor(palette.text);moduleSearch.extractRenderState(g,mx,my,0);
    }
    private void drawEmptySearch(GuiGraphicsExtractor g,int mx,int my){
        int w=Math.min(480,moduleWidth-48),h=Math.min(176,ph-168),x=px+(moduleWidth-w)/2,y=py+112;
        UiPaint.surface(g,x,y,w,h,10,palette.inset,palette.border,opacity);
        boolean roomy=h>=144&&w>=310;
        if(roomy){
            UiPaint.rounded(g,x+16,y+16,32,32,8,UiPaint.alpha(palette.selected,opacity));
            UiPaint.icon(g,UiPaint.Icon.SEARCH,x+24,y+24,16,palette.accentText,opacity);
        }
        float titleX=x+(roomy?60:16);
        float titleScale=h<116?.88f:.95f;
        UiPaint.baseline(g,UiPaint.fit(g,tr("module_search.empty"),x+w-16-titleX,titleScale),titleX,
                UiPaint.textBaseline(g,y+(h<116?8:14),h<116?22:30,titleScale),titleScale,palette.text,opacity);
        if(h>=116){
            int lineY=y+(roomy?57:45),bottom=feedbackButton.getY()-9;
            float scale=w<310?.72f:.8f;
            String text=tr("module_search.contact").getString();
            var lines=new ArrayList<String>();String line="";
            for(String word:text.split("\\s+")){
                String next=line.isEmpty()?word:line+" "+word;
                if(!line.isEmpty()&&UiPaint.textWidth(g,next,scale)>w-32){lines.add(line);line=word;}else line=next;
            }
            if(!line.isEmpty())lines.add(line);
            int maximum=Math.max(1,(bottom-lineY)/18);
            for(int i=0;i<Math.min(maximum,lines.size());i++)UiPaint.baseline(g,
                    UiPaint.fit(g,Component.literal(lines.get(i)),w-32,scale),x+16,UiPaint.textBaseline(g,lineY+i*18,18,scale),scale,palette.muted,opacity);
        }
        feedbackButton.frame(palette,opacity,elapsed);feedbackButton.extractRenderState(g,mx,my,0);
    }
    private void drawPane(GuiGraphicsExtractor g, int mx, int my, float alpha) {
        UiPaint.baseline(g, UiPaint.fit(g, Component.literal(selectedModule.title), paneWidth - 82, 1), paneX,
                UiPaint.textBaseline(g, paneTop, 26, 1), 1, palette.text, alpha);
        if (!denseFilters() && !(crosshair() && ph < 380)) UiPaint.baseline(g, UiPaint.fit(g, selectedModule.description(), paneWidth - 2, .8f), paneX,
                UiPaint.textBaseline(g, paneTop + 31, 21, .8f), .8f, palette.muted, alpha);
        if (crosshair()) drawCrosshairPreview(g, alpha);
        if (noRender()) {
            UiPaint.surface(g,paneX,paneTop+searchOffset(),paneWidth,30,6,palette.field,search.isFocused()?palette.accent:palette.border,alpha);
            UiPaint.icon(g, UiPaint.Icon.SEARCH, paneX + 9, paneTop + searchOffset() + 8, 13, palette.faint, alpha);
            if (search.getValue().isEmpty()) {
                g.pose().pushMatrix(); g.pose().translate(0, UiFont.centeredTextOffset(search.getHeight()));
                g.text(UiFont.get(), tr("ui.search"), search.getX(), search.getY(), UiPaint.alpha(palette.faint, alpha), false);
                g.pose().popMatrix();
            }
            search.setTextColor(UiPaint.alpha(palette.text, alpha)); search.setAlpha(alpha); search.extractRenderState(g, mx, my, 0);
        }
        drawButtons(g, paneControls, expandedChoice == null ? mx : -1000, my, alpha);
        g.enableScissor(listX, listY, listX + listWidth, listBottom);
        for (var row : rows) {
            if(!noRender()&&row.visible)UiPaint.rounded(g,row.getX(),row.getY(),row.getWidth(),54,7,UiPaint.alpha(palette.raised,alpha*.75f));
            if (row instanceof VisualsButton button) button.frame(palette, alpha, elapsed);
            row.extractRenderState(g, expandedChoice == null ? mx : -1000, my >= listY && my < listBottom ? my : -1000, 0);
        }
        g.disableScissor();
        if (noRender() && filtered.isEmpty()) UiPaint.centered(g, tr("no_results").getString(), listX, listY + 12, listWidth, 30, .9f, palette.muted, alpha);
        drawScrollbar(g, paneX + paneWidth - 3, listY, listBottom - listY, scroll.value(), maxScroll(), alpha);
        g.fill(paneX, paneBottom - 34, paneX + paneWidth, paneBottom - 33, UiPaint.alpha(palette.border, alpha * .45f));
    }
    private int previewTop() { return paneTop + (ph < 380 ? 34 : 61); }
    private void drawCrosshairPreview(GuiGraphicsExtractor g, float alpha) {
        int top = previewTop(), h = listY - top - 10;
        UiPaint.rounded(g, paneX, top, paneWidth, h, 8, UiPaint.alpha(0xff263b43, alpha));
        g.enableScissor(paneX, top, paneX + paneWidth, top + h);
        UiPaint.rounded(g, paneX + paneWidth / 2f, top, paneWidth / 2f, h, 0, UiPaint.alpha(0xffaeb6b1, alpha));
        float attack = Math.clamp((System.nanoTime() - crosshairPreviewStart) / 900_000_000f, 0, 1);
        float radius = config.number(VisualSetting.CROSSHAIR_SIZE) + config.number(VisualSetting.CROSSHAIR_GAP)
                + (config.flag(VisualSetting.CROSSHAIR_DYNAMIC) ? config.number(VisualSetting.CROSSHAIR_SPREAD) : 0)
                + config.number(VisualSetting.CROSSHAIR_THICKNESS) / 2 + config.number(VisualSetting.CROSSHAIR_OUTLINE_WIDTH);
        if(config.value(VisualSetting.CROSSHAIR_STYLE)==3)radius=17*config.number(VisualSetting.CROSSHAIR_CUSTOM_SCALE)*(1+config.number(VisualSetting.CROSSHAIR_SPREAD)/16);
        float zoom = Math.max(1, Math.min(3, (h - 22) / (radius * 2)));
        for (int side = 0; side < 2; side++) {
            float cx = paneX + paneWidth * (.25f + side * .5f), cy = top + (h - 14) / 2f;
            g.pose().pushMatrix(); g.pose().translate(cx, cy);
            float scale = (side == 0 ? 1 : zoom) / UiSprites.pixelsPerUnit(g);
            g.pose().scale(scale, scale); CrosshairPainter.draw(g, 0, 0, attack, alpha); g.pose().popMatrix();
        }
        UiPaint.centered(g, tr("crosshair.preview").getString(), paneX, top + h - 21, paneWidth, 20, .7f, 0xfff0f3f1, alpha);
        g.disableScissor();
    }
    private void drawScrollbar(GuiGraphicsExtractor g, float x, float y, float track, float value, float maximum, float alpha) {
        if (maximum <= 0) return; float thumb = Math.max(20, track * track / (track + maximum));
        UiPaint.rounded(g, x, y, 2, track, 1, UiPaint.alpha(palette.track, alpha * .45f));
        UiPaint.rounded(g, x, y + (track - thumb) * value / maximum, 2, thumb, 1, UiPaint.alpha(palette.accent, alpha * .8f));
    }
    private void drawButtons(GuiGraphicsExtractor g, List<Slot> buttons, int mx, int my, float alpha) {
        for (Slot slot : buttons) { slot.button.frame(palette, alpha, elapsed); slot.button.extractRenderState(g, mx, my, 0); }
    }
    private void drawHint(GuiGraphicsExtractor g, int mx, int my) {
        VisualsButton hovered = null;
        for (Slot slot : chrome) if (slot.button.showsHint()) hovered = slot.button;
        for (Slot slot : modules) if (slot.button.showsHint()) hovered = slot.button;
        for (Slot slot : paneControls) if (slot.button.showsHint()) hovered = slot.button;
        for (var row : rows) if (row instanceof VisualsButton b && b.showsHint() && my >= listY && my < listBottom) hovered = b;
        if (hovered == null) return;
        String text = UiPaint.fit(g, hovered.detail, Math.min(340, viewport.width - 44), .8f);
        float w = UiPaint.textWidth(g, text, .8f) + 22;
        float x = Math.clamp(mx + 12, 8, viewport.width - w - 8), y = Math.clamp(my + 18, 8, viewport.height - 37);
        g.nextStratum(); UiPaint.rounded(g, x, y, w, 28, 6, palette.hover);
        UiPaint.baseline(g, text, x + 11, UiPaint.textBaseline(g, y, 28, .8f), .8f, palette.text, opacity);
    }
    private void showChoice(VisualSetting setting, AbstractWidget anchor) {
        if (expandedChoice == setting) { closeChoice(); return; }
        closeChoice(); expandedChoice = setting; popupWidth = paneWidth - 4;
        popupRows=Math.min(setting.choices.length,Math.max(1,Math.min(8,(ph-90)/30)));popupHeight=popupRows*30+10;
        popupFirst=Math.clamp((int)config.value(setting)-popupRows/2,0,Math.max(0,setting.choices.length-popupRows));
        popupX = paneX; popupY = Math.clamp(anchor.getY() + anchor.getHeight() - 1, py + 56, Math.max(py + 56, py + ph - popupHeight - 12));
        for (int i = 0; i < setting.choices.length; i++) {
            int index = i;
            popup.add(addWidget(new VisualsButton(popupX + 5, popupY + 5 + i * 30, popupWidth - 10, 30,
                    setting.formatted(i), MENU, null, Component.empty(), () -> {
                        config.setValue(setting, index); closeChoice(); if (crosshair() || setting == VisualSetting.FREE_LOOK_MODE||setting==VisualSetting.ZOOM_RANGE_MODE||setting==VisualSetting.DROP_MODE) refreshCrosshairRows();
                    }, () -> (int)config.value(setting) == index)));
        }
        positionChoice();
        setFocused(popup.get(Math.clamp((int)config.value(setting), 0, popup.size() - 1)));
    }
    private void positionChoice(){
        for(int index=0;index<popup.size();index++){
            var button=popup.get(index);button.setY(popupY+5+(index-popupFirst)*30);
            button.visible=index>=popupFirst&&index<popupFirst+popupRows;
        }
    }
    private void revealChoice(int index){
        if(index<popupFirst)popupFirst=index;
        else if(index>=popupFirst+popupRows)popupFirst=index-popupRows+1;
        positionChoice();
    }
    private void closeChoice() {
        if (popup.contains(getFocused())) setFocused(null);
        popup.forEach(this::removeWidget); popup.clear(); expandedChoice = null;
    }
    private boolean inPopup(double x, double y) { return x >= popupX && x < popupX + popupWidth && y >= popupY && y < popupY + popupHeight; }
    private void drawPopup(GuiGraphicsExtractor g, int mx, int my) {
        g.nextStratum(); UiPaint.shadow(g, popupX, popupY, popupWidth, popupHeight, 8, opacity);
        UiPaint.rounded(g, popupX - 1, popupY - 1, popupWidth + 2, popupHeight + 2, 9, palette.border);
        UiPaint.rounded(g, popupX, popupY, popupWidth, popupHeight, 8, palette.raised);
        g.enableScissor(popupX,popupY+4,popupX+popupWidth,popupY+popupHeight-4);
        for (var button : popup) { button.frame(palette, opacity, elapsed); button.extractRenderState(g, mx, my, 0); }
        g.disableScissor();
        if(popup.size()>popupRows){float track=popupHeight-10,thumb=Math.max(12,track*popupRows/popup.size());float top=popupY+5+(track-thumb)*popupFirst/(popup.size()-popupRows);UiPaint.rounded(g,popupX+popupWidth-4,top,2,thumb,1,UiPaint.alpha(palette.muted,opacity));}
    }
    private MouseButtonEvent adjusted(MouseButtonEvent event) {
        return new MouseButtonEvent(event.x() / viewport.scale, event.y() / viewport.scale - (1 - opening.value()) * 6, event.buttonInfo());
    }
    @Override public void mouseMoved(double x, double y) { var p = adjusted(new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0))); super.mouseMoved(p.x(), p.y()); }
    @Override public boolean mouseClicked(MouseButtonEvent raw, boolean twice) {
        if (closing) return true; var event = adjusted(raw);
        if (expandedChoice != null) {
            if (!inPopup(event.x(), event.y())) { closeChoice(); return true; }
            for (var button : List.copyOf(popup)) if (button.mouseClicked(event, twice)) return true;
            return true;
        }
        if (crosshair() && event.button() == InputConstants.MOUSE_BUTTON_LEFT && event.x() >= paneX && event.x() < paneX + paneWidth
                && event.y() >= previewTop() && event.y() < listY - 10) { crosshairPreviewStart = System.nanoTime(); return true; }
        if (selectedModule != null && event.button() == InputConstants.MOUSE_BUTTON_LEFT && maxScroll() > 0
                && event.x() >= paneX + paneWidth - 9 && event.x() <= paneX + paneWidth + 3 && event.y() >= listY && event.y() < listBottom) {
            grabScroll((float)event.y(), false); return true;
        }
        if (maxModuleScroll() > 0 && event.x() >= px + moduleWidth - 17 && event.x() < px + moduleWidth - 5
                && event.y() >= py + 104 && event.y() < py + ph - 48) { grabScroll((float)event.y(), true); return true; }
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && event.y() >= py + 102 && event.y() < py + ph - 48) {
            for (var entry : moduleIds.entrySet()) {
                var button = entry.getKey();
                if (button.visible && event.x() >= button.getX() && event.x() < button.getX() + button.getWidth() + 32
                        && event.y() >= button.getY() && event.y() < button.getY() + button.getHeight()) { openSettings(entry.getValue()); return true; }
            }
        }
        if (event.y() < py + 102 || event.y() >= py + ph - 48) modules.forEach(slot -> slot.button.visible = false);
        if (event.y() < listY || event.y() >= listBottom) rows.forEach(row -> row.visible = false);
        boolean handled = super.mouseClicked(event, twice);
         
        if (expandedChoice != null) setFocused(popup.get((int)config.value(expandedChoice)));
        else if (getFocused() != null && !children().contains(getFocused())) setFocused(null);
        positionWidgets(); return handled;
    }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        var event = adjusted(new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0)));
        if (expandedChoice != null) {
            if(inPopup(event.x(),event.y())){popupFirst=Math.clamp(popupFirst-(int)Math.signum(vertical)*2,0,Math.max(0,popup.size()-popupRows));positionChoice();}
            return true;
        }
        if (selectedModule != null && event.x() >= paneX && event.x() < paneX + paneWidth && event.y() >= listY && event.y() < listBottom) {
            scrollTarget = Math.clamp(scrollTarget - (float)vertical * 44, 0, maxScroll()); return true;
        }
        if (!(compact && selectedModule != null) && event.x() >= px && event.x() < px + moduleWidth && event.y() >= py + 102 && event.y() < py + ph - 48) {
            moduleScrollTarget = Math.clamp(moduleScrollTarget - (float)vertical * 44, 0, maxModuleScroll()); return true;
        }
        return false;
    }
    private void grabScroll(float y, boolean main) {
        float track = main ? ph - 152 : listBottom - listY, maximum = main ? maxModuleScroll() : maxScroll();
        float value = main ? moduleScroll.value() : scroll.value(), top = main ? py + 104 : listY;
        float thumb = Math.max(20, track * track / (track + maximum)), currentTop = top + (track - thumb) * value / maximum;
        scrollbarGrab = y >= currentTop && y < currentTop + thumb ? y - currentTop : thumb / 2;
        draggingModules = main; draggingScroll = !main; dragScroll(y);
    }
    private void dragScroll(double y) {
        boolean main = draggingModules;
        float track = main ? ph - 152 : listBottom - listY, maximum = main ? maxModuleScroll() : maxScroll();
        float thumb = Math.max(20, track * track / (track + maximum));
        float value = Math.clamp((float)(y - (main ? py + 104 : listY) - scrollbarGrab) / Math.max(1, track - thumb) * maximum, 0, maximum);
        if (main) { moduleScrollTarget = value; moduleScroll.snap(value); } else { scrollTarget = value; scroll.snap(value); }
        positionWidgets();
    }
    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingScroll || draggingModules) { dragScroll(adjusted(event).y()); return true; }
        return super.mouseDragged(adjusted(event), dx / viewport.scale, dy / viewport.scale);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) { draggingScroll = draggingModules = false; return super.mouseReleased(adjusted(event)); }
    @Override public boolean keyPressed(KeyEvent event) {
        if (expandedChoice != null && event.key() == InputConstants.KEY_ESCAPE) { closeChoice(); return true; }
        boolean typing = search != null && search.isFocused() || protectedNameField != null && protectedNameField.isFocused()||moduleSearch!=null&&moduleSearch.isFocused();
        if(event.key()==InputConstants.KEY_ESCAPE&&moduleSearch!=null&&moduleSearch.isFocused()&&!moduleQuery.isBlank()){moduleSearch.setValue("");return true;}
        if (ToolVisualsClient.openMenuKey != null && ToolVisualsClient.openMenuKey.matches(event)
                && (!typing || event.key() == InputConstants.KEY_RSHIFT)) { closeMenu(); return true; }
        if (protectedNameField != null && protectedNameField.isFocused() && event.isConfirmation()) { setFocused(null); return true; }
        if(GuiMovement.consumeKey(minecraft,event))return true;
        if (expandedChoice != null) {
            int key = event.key(), index = popup.indexOf(getFocused());
            if (index < 0) index = (int)config.value(expandedChoice);
            if (key == InputConstants.KEY_DOWN || key == InputConstants.KEY_UP || key == InputConstants.KEY_TAB
                    || key == InputConstants.KEY_HOME || key == InputConstants.KEY_END) {
                if (key == InputConstants.KEY_HOME) index = 0;
                else if (key == InputConstants.KEY_END) index = popup.size() - 1;
                else index = Math.floorMod(index + (key == InputConstants.KEY_UP || key == InputConstants.KEY_TAB && event.hasShiftDown() ? -1 : 1), popup.size());
                revealChoice(index);setFocused(popup.get(index)); return true;
            }
            if (event.isConfirmation()) popup.get(index).keyPressed(event);
            return true;
        }
        if (event.hasControlDown() && event.key() == InputConstants.KEY_F) {
            if(moduleSearch==null){selectedModule=null;rebuildWidgets();}
            setFocused(moduleSearch);moduleSearch.setFocused(true);return true;
        }
        if (event.hasShiftDown() && event.isConfirmation())
            for (var entry : moduleIds.entrySet()) if (entry.getKey().isFocused()) { openSettings(entry.getValue()); return true; }
        if (selectedModule != null && !typing && (event.key() == InputConstants.KEY_PAGEUP || event.key() == InputConstants.KEY_PAGEDOWN)) {
            scrollTarget = Math.clamp(scrollTarget + (event.key() == InputConstants.KEY_PAGEUP ? -1 : 1) * (listBottom - listY), 0, maxScroll()); return true;
        }
        return super.keyPressed(event);
    }
    private void closeMenu() { closing = true; if (search != null) search.setFocused(false);if(moduleSearch!=null)moduleSearch.setFocused(false); if (protectedNameField != null) protectedNameField.setFocused(false); closeChoice(); }
    @Override public void onClose() { if (expandedChoice != null) closeChoice(); else if (selectedModule != null) closeSettings(); else closeMenu(); }
    @Override public boolean isPauseScreen() { return false; }

    private final class ProtectedNameField extends EditBox {
        ProtectedNameField() {
            super(UiFont.get(),0,0,paneWidth-24,54,tr("setting.protected_name"));
            setBordered(false); setTextShadow(false); setMaxLength(32); setValue(config.protectedName());
            setResponder(config::setProtectedName);
        }
        @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
            float alpha = opacity * paneOpening.value();
            UiPaint.baseline(g,UiPaint.fit(g,getMessage(),getWidth(),.9f),getX(),
                    UiPaint.textBaseline(g,getY(),22,.9f),.9f,palette.text,alpha);
            UiPaint.rounded(g,getX()-8,getY()+24,getWidth()+16,30,6,UiPaint.alpha(isFocused()?palette.hover:palette.field,alpha));
            if (getValue().isEmpty() && !isFocused()) {
                UiPaint.baseline(g,"ToolVisuals",getX(),UiPaint.textBaseline(g,getY()+24,30,1),1,palette.muted,alpha);return;
            }
            setTextColor(UiPaint.alpha(palette.text,alpha));setAlpha(alpha);
            g.pose().pushMatrix();g.pose().translate(0,UiFont.centeredTextOffset(getHeight())+12);
            super.extractWidgetRenderState(g,mx,my,partial);g.pose().popMatrix();
        }
    }

    private final class ColorButton extends VisualsButton {
        private final VisualSetting setting;
        ColorButton(VisualSetting setting){super(0,0,paneWidth-8,50,setting.label(),GHOST,null,Component.empty(),()->{},null);this.setting=setting;}
        @Override public void onPress(net.minecraft.client.input.InputWithModifiers input){minecraft.gui.setScreen(new ColorEditorScreen(VisualsScreen.this,setting.label(),config.colorStyle(setting),style->config.setColorStyle(setting,style),setting==VisualSetting.CROSSHAIR_COLOR&&(config.value(VisualSetting.CROSSHAIR_STYLE)==0||config.value(VisualSetting.CROSSHAIR_STYLE)==3)));}
        @Override public void onClick(MouseButtonEvent e,boolean twice){if(setting==VisualSetting.SKY_TINT&&e.x()>getX()+getWidth()-40){config.setSkyTintEnabled(config.value(setting)==0);return;}super.onClick(e,twice);}
        @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial){
            isHovered=UiPaint.hovered(g,this,mx,my);float alpha=opacity*paneOpening.value();int reserve=setting==VisualSetting.SKY_TINT?44:0;
            UiPaint.baseline(g,UiPaint.fit(g,setting.label(),getWidth()-16-reserve,.9f),getX()+8,UiPaint.textBaseline(g,getY(),23,.9f),.9f,palette.text,alpha);
            var style=config.colorStyle(setting);int w=getWidth()-16-reserve;
            UiPaint.rounded(g,getX()+7,getY()+26,w+2,23,6,UiPaint.alpha(isHovered()?palette.accent:palette.border,alpha));
            for(int i=0;i<w;i+=2)g.fill(getX()+8+i,getY()+28,getX()+8+Math.min(w,i+2),getY()+47,UiPaint.alpha(0xff000000|style.sample(ColorStyle.clock(),i/(double)Math.max(1,w-1)),alpha));
            if(setting==VisualSetting.SKY_TINT)UiPaint.toggle(g,getX()+getWidth()-34,getY()+28,config.value(setting)>0?1:0,palette,alpha);
        }
    }
    private final class ChoiceButton extends VisualsButton {
        private final VisualSetting setting;
        ChoiceButton(VisualSetting setting) {
            super(0, 0, paneWidth - 8, 50, setting.label(), GHOST, null, Component.empty(), () -> {}, null); this.setting = setting;
        }
        @Override public void onPress(net.minecraft.client.input.InputWithModifiers input) { showChoice(setting, this); }
        @Override protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float partial) {
            isHovered = UiPaint.hovered(g, this, mx, my);
            float alpha = opacity * paneOpening.value(); String value = setting.formatted(config.value(setting)).getString();
            float valueWidth = Math.min(getWidth() - 20, UiPaint.textWidth(g, value, .85f) + 32);
            UiPaint.baseline(g, UiPaint.fit(g, setting.label(), getWidth() - 16, .9f), getX() + 8,
                    UiPaint.textBaseline(g, getY(), 23, .9f), .9f, palette.text, alpha);
            UiPaint.rounded(g, getX() + 8, getY() + 26, valueWidth, 23, 5,
                    UiPaint.alpha(isHoveredOrFocused() || expandedChoice == setting ? palette.hover : palette.field, alpha));
            UiPaint.baseline(g, value, getX() + 17, UiPaint.textBaseline(g, getY() + 26, 23, .85f), .85f, palette.accentText, alpha);
            g.pose().pushMatrix(); g.pose().translate(getX() + 8 + valueWidth - 13, getY() + 37.5f); g.pose().rotate((float)Math.PI / 2);
            UiPaint.icon(g, UiPaint.Icon.ARROW, -5, -5, 10, palette.muted, alpha); g.pose().popMatrix();
        }
        @Override protected net.minecraft.network.chat.MutableComponent createNarrationMessage() { return setting.label().copy().append(": ").append(setting.formatted(config.value(setting))); }
    }
    private final class SettingSlider extends AbstractSliderButton {
        private final VisualSetting setting;
        SettingSlider(VisualSetting setting) {
            super(0, 0, paneWidth - 8, 50, setting.label(), (config.value(setting) - setting.min) / (setting.max - setting.min)); this.setting = setting; updateMessage();
        }
        @Override protected void updateMessage() { if (setting != null) setMessage(setting.label().copy().append(": ").append(setting.formatted(config.value(setting)))); }
        @Override protected void applyValue() { config.setValue(setting, setting.min + value * (setting.max - setting.min)); updateMessage(); }
        @Override public void extractWidgetRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
            float alpha = opacity * paneOpening.value(), baseline = UiPaint.textBaseline(g, getY(), 24, .9f);
            String text = setting.formatted(config.value(setting)).getString(); float valueWidth = UiPaint.textWidth(g, text, .9f);
            UiPaint.baseline(g, UiPaint.fit(g, setting.label(), getWidth() - valueWidth - 30, .9f), getX() + 8, baseline, .9f, palette.text, alpha);
            UiPaint.baseline(g, text, getX() + getWidth() - 8 - valueWidth, baseline, .9f, palette.accentText, alpha);
            float left = getX() + 8, length = getWidth() - 16, progress = (float)value;
            UiPaint.rounded(g, left, getY() + 37, length, 3, 1.5f, UiPaint.alpha(palette.track, alpha));
            UiPaint.rounded(g, left, getY() + 37, length * progress, 3, 1.5f, UiPaint.alpha(palette.accent, alpha));
            UiPaint.rounded(g, left + length * progress - 4.5f, getY() + 34, 9, 9, 4.5f, UiPaint.alpha(palette.text, alpha));
        }
    }
}
