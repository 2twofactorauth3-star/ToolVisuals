package ru.he3hauka.toolvisuals.client.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.he3hauka.toolvisuals.client.chat.layout.ChatSize;
import ru.he3hauka.toolvisuals.client.connection.policy.ClientFeature;
import ru.he3hauka.toolvisuals.client.connection.policy.FeatureCatalog;
import ru.he3hauka.toolvisuals.client.connection.policy.ServerFeaturePolicy;
import ru.he3hauka.toolvisuals.client.config.color.ColorSettings;
import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.crosshair.CrosshairDesign;
import ru.he3hauka.toolvisuals.client.hud.layout.HudElement;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPosition;
import ru.he3hauka.toolvisuals.client.hud.layout.resize.HudScale;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

public final class ClientConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("ToolVisuals");
    private final Path path;
    private final ru.he3hauka.toolvisuals.client.storage.JsonFile file;
    private final ru.he3hauka.toolvisuals.client.storage.DeferredJson persistence;
    public ru.he3hauka.toolvisuals.client.storage.JsonFile.Status persistenceStatus(){return file.status();}
    public void flush() throws IOException {persistence.flush();}
    public final ConfigHistory history=new ConfigHistory();
    private boolean restoringHistory;
    private volatile long revision;
    public final ServerFeaturePolicy serverPolicy = new ServerFeaturePolicy();
    public long revision() { return revision + serverPolicy.revision(); }
    public boolean available(VisualModule module) {
        return serverPolicy.allows(module.id)
                && (module != VisualModule.FRIENDS_LIST || available(ClientFeature.FRIENDS));
    }
    public boolean available(ClientFeature feature) {
        return serverPolicy.allows(feature.id) && (feature != ClientFeature.OUTFITS
                || available(VisualModule.CAPES) && available(VisualModule.COSMETICS));
    }
    public boolean available(RenderOption option) {
        return available(VisualModule.NO_RENDER) && serverPolicy.allows(FeatureCatalog.option(option));
    }
    public boolean available(VisualSetting setting) {
        return available(setting.module) && (setting != VisualSetting.NO_SWING && setting != VisualSetting.HAND_NO_SWING
                && setting != VisualSetting.LEFT_HAND_NO_SWING || available(VisualModule.NO_SWING_ANIMATION));
    }
     
    private volatile EnumSet<RenderOption> hidden = defaults();
    private volatile boolean enabled;
    private int menuBindingVersion;
    private boolean lightTheme;
    private boolean customTitleMenu;
    private volatile ChatSize chatSize=ChatSize.DEFAULT;
    public ChatSize chatSize(){return chatSize;}
    public void setChatSize(double width,double height){
        var size=new ChatSize(width,height);if(size.equals(chatSize))return;
        chatSize=size;save("chat_size");
    }
    public boolean customTitleMenu() { return customTitleMenu; }
    public void setCustomTitleMenu(boolean custom) { customTitleMenu=custom;save(); }
    private UiLanguage language = UiLanguage.ENGLISH;
    private String nickname = "";
    private volatile String protectedName = "ToolVisuals";
    private volatile EnumSet<VisualModule> modules = ConfigMigrations.defaultModules();
    private volatile Map<VisualSetting, Double> values = Map.of();
    private volatile Map<VisualSetting, ColorStyle> colors = Map.of();
    private volatile CrosshairDesign crosshairDesign = CrosshairDesign.cross();
    private volatile ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook accessories=ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook.EMPTY;
    public ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook accessories(){return accessories;}
    public void setAccessories(ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook look){accessories=java.util.Objects.requireNonNull(look);save();}
    public void applyAppearance(ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook look,boolean cape){
        accessories=java.util.Objects.requireNonNull(look);var next=modules.clone();
        if(!look.empty())next.add(VisualModule.COSMETICS);
        if(cape)next.add(VisualModule.CAPES);
        modules=next;save("appearance");
    }
    public CrosshairDesign crosshairDesign() { return crosshairDesign; }
    public ColorStyle colorStyle(VisualSetting setting) {
        if (!ColorSettings.contains(setting)) throw new IllegalArgumentException("Not a color setting");
        var style = colors.get(setting);
        return style == null ? ColorStyle.solid(ColorSettings.legacy(this,setting)) : style;
    }
    public int color(VisualSetting setting) { return colorStyle(setting).sample(ColorStyle.clock(),0); }
    public void setSkyTintEnabled(boolean enabled) {var next=new EnumMap<VisualSetting,Double>(VisualSetting.class);next.putAll(values);next.put(VisualSetting.SKY_TINT,enabled?1d:0d);values=Map.copyOf(next);save();}
    public void setColorStyle(VisualSetting setting,ColorStyle style) {
        if (!ColorSettings.contains(setting)) throw new IllegalArgumentException("Not a color setting");
        var next=new EnumMap<VisualSetting,ColorStyle>(VisualSetting.class);next.putAll(colors);next.put(setting,ColorStyle.safe(style,ColorSettings.legacy(this,setting)));colors=Map.copyOf(next);
        if(setting==VisualSetting.SKY_TINT&&value(setting)==0){var v=new EnumMap<VisualSetting,Double>(VisualSetting.class);v.putAll(values);v.put(setting,1d);values=Map.copyOf(v);}
        save("color:"+setting.id);
    }
    public void setCrosshairDesign(CrosshairDesign design,ColorStyle style) {
        crosshairDesign=java.util.Objects.requireNonNull(design);
        var c=new EnumMap<VisualSetting,ColorStyle>(VisualSetting.class);c.putAll(colors);c.put(VisualSetting.CROSSHAIR_COLOR,ColorStyle.safe(style,0xffffff));colors=Map.copyOf(c);
        var v=new EnumMap<VisualSetting,Double>(VisualSetting.class);v.putAll(values);v.put(VisualSetting.CROSSHAIR_STYLE,3d);values=Map.copyOf(v);
        var nextModules=modules.clone();nextModules.add(VisualModule.CROSSHAIR);modules=nextModules;save("crosshair_design");
    }
    private volatile Map<HudElement, HudPosition> hudPositions = Map.of();
    private volatile Map<HudElement, Double> hudScales = Map.of();
    public Map<HudElement, Double> hudScales(){return hudScales;}
    public double hudScale(HudElement element){return hudScales.getOrDefault(element,1d);}
    public void resizeHud(HudElement element,double scale,HudPosition position){
        var nextScales=new EnumMap<HudElement,Double>(HudElement.class);nextScales.putAll(hudScales);
        scale=HudScale.sanitize(scale);if(scale==1)nextScales.remove(element);else nextScales.put(element,scale);
        var nextPositions=new EnumMap<HudElement,HudPosition>(HudElement.class);nextPositions.putAll(hudPositions);
        nextPositions.put(element,new HudPosition(position.x,position.y));
        applyHudState(nextPositions,nextScales,false,"hud_resize:"+element.id);
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(element.module,"hud_resized","element="+element.id+" scale="+scale);
    }
    private volatile HudPosition hudEditorToolbar=new HudPosition(50,98);
    public HudPosition hudEditorToolbar() { return hudEditorToolbar; }
    public void setHudEditorToolbar(HudPosition position) {
        hudEditorToolbar=new HudPosition(position.x,position.y);save("hud_toolbar");
    }
    private volatile Set<VisualModule> favorites = Set.of();
    public boolean favorite(VisualModule module) { return favorites.contains(module); }
    public void toggleFavorite(VisualModule module) {
        var next = EnumSet.noneOf(VisualModule.class); next.addAll(favorites);
        if (!next.remove(module)) next.add(module);
        favorites = Set.copyOf(next); save();
    }
    private volatile java.util.List<String> pvpBlockedCommands = ru.he3hauka.toolvisuals.client.pvp.CombatCommands.DEFAULTS;
    public java.util.List<String> pvpBlockedCommands() { return pvpBlockedCommands; }
    public void setPvpBlockedCommands(Collection<String> commands) {
        var next = ru.he3hauka.toolvisuals.client.pvp.CombatCommands.normalize(commands);
        if (next.equals(pvpBlockedCommands)) return;
        pvpBlockedCommands = next; save();
    }
    public Map<HudElement, HudPosition> hudPositions() { return hudPositions; }
    public void applyHudLayout(Map<HudElement, HudPosition> positions, boolean resetDefaults) {
        applyHudLayout(positions,resetDefaults?Map.of():hudScales,resetDefaults);
    }
    public void applyHudLayout(Map<HudElement,HudPosition> positions,Map<HudElement,Double> scales,boolean resetDefaults){
        applyHudState(positions,scales,resetDefaults,"hud_layout");
    }
    private void applyHudState(Map<HudElement,HudPosition> positions,Map<HudElement,Double> scales,boolean resetDefaults,String group){
        var nextValues = new EnumMap<VisualSetting, Double>(VisualSetting.class); nextValues.putAll(values);
        if (resetDefaults) for (var element : HudElement.values()) if (element.xSetting != null) {
            nextValues.remove(element.xSetting); nextValues.remove(element.ySetting);
        }
        var next = new EnumMap<HudElement, HudPosition>(HudElement.class);
        positions.forEach((element, position) -> {
            if (element == null || position == null) return;
            var safe = new HudPosition(position.x, position.y); next.put(element, safe);
            if (element.xSetting != null) {
                nextValues.put(element.xSetting, element.xSetting.sanitize(safe.x));
                nextValues.put(element.ySetting, element.ySetting.sanitize(safe.y));
            }
        });
        var nextScales=new EnumMap<HudElement,Double>(HudElement.class);
        scales.forEach((element,scale)->{if(element!=null&&scale!=null&&HudScale.sanitize(scale)!=1)nextScales.put(element,HudScale.sanitize(scale));});
        hudPositions = Map.copyOf(next);hudScales=Map.copyOf(nextScales);values = Map.copyOf(nextValues); save(group);
    }

    public boolean active(VisualModule module) {
        boolean active=available(module) && (module == VisualModule.NO_RENDER ? enabled : modules.contains(module));
        ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.checked(this,module,active);
        return active;
    }

    public void setActive(VisualModule module, boolean active) {
        if (!available(module)) return;
        if (module == VisualModule.NO_RENDER) { setEnabled(active); return; }
        boolean changed=modules.contains(module)!=active;
        var next = modules.clone();
        if (active) next.add(module); else next.remove(module);
        modules = next;
        if(changed&&ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(module,"toggle","enabled="+active);
        save();
    }

    public double value(VisualSetting setting) { return values.getOrDefault(setting, setting.initial); }
    public float number(VisualSetting setting) { return (float)value(setting); }
    public boolean flag(VisualSetting setting) { return available(setting) && value(setting) > .5; }

    public void setValue(VisualSetting setting, double value) {
        setValues(Map.of(setting,value),"setting:"+setting.id);
    }

    public void setValues(Map<VisualSetting,Double> changes,String historyGroup) {
        var next=new EnumMap<VisualSetting,Double>(VisualSetting.class);next.putAll(values);
        EnumMap<VisualSetting,ColorStyle> nextColors=null;
        EnumMap<HudElement,HudPosition> positions=null;
        boolean changed=false;
        for(var entry:changes.entrySet()) {
            var setting=java.util.Objects.requireNonNull(entry.getKey());double value=setting.sanitize(entry.getValue());
            if(Double.compare(value(setting),value)!=0){next.put(setting,value);changed=true;}
            var color=ColorSettings.contains(setting)?setting:
                    setting==VisualSetting.CROSSHAIR_RED||setting==VisualSetting.CROSSHAIR_GREEN||setting==VisualSetting.CROSSHAIR_BLUE?VisualSetting.CROSSHAIR_COLOR:null;
            if(color!=null&&colors.containsKey(color)) {
                if(nextColors==null){nextColors=new EnumMap<>(VisualSetting.class);nextColors.putAll(colors);}
                nextColors.remove(color);changed=true;
            }
            for(var element:HudElement.values())if((setting==element.xSetting||setting==element.ySetting)&&hudPositions.containsKey(element)) {
                if(positions==null){positions=new EnumMap<>(HudElement.class);positions.putAll(hudPositions);}
                positions.remove(element);changed=true;
            }
        }
        if(!changed)return;
        values=Map.copyOf(next);
        if(nextColors!=null)colors=Map.copyOf(nextColors);
        if(positions!=null)hudPositions=Map.copyOf(positions);
        save(historyGroup);
    }

    public void reset(VisualModule module) {
        if(module==VisualModule.COSMETICS)accessories=ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook.EMPTY;
        if (module == VisualModule.NO_RENDER) { reset(); return; }
        if (module == VisualModule.NAME_PROTECT) protectedName = "ToolVisuals";
        if (module == VisualModule.PVP_SAVE) pvpBlockedCommands = ru.he3hauka.toolvisuals.client.pvp.CombatCommands.DEFAULTS;
        if (module == VisualModule.CROSSHAIR) crosshairDesign = CrosshairDesign.cross();
        var nextColors=new EnumMap<VisualSetting,ColorStyle>(VisualSetting.class);colors.forEach((setting,color)->{if(setting.module!=module)nextColors.put(setting,color);});colors=Map.copyOf(nextColors);
        var positions = new EnumMap<HudElement, HudPosition>(HudElement.class);
        hudPositions.forEach((element, position) -> { if (element.module != module) positions.put(element, position); });
        hudPositions = Map.copyOf(positions);
        var scales=new EnumMap<HudElement,Double>(HudElement.class);hudScales.forEach((element,scale)->{if(element.module!=module)scales.put(element,scale);});hudScales=Map.copyOf(scales);
        var next = new EnumMap<VisualSetting, Double>(VisualSetting.class);
        values.forEach((setting, value) -> { if (setting.module != module) next.put(setting, value); });
        values = Map.copyOf(next);
        setActive(module, ConfigMigrations.defaultModules().contains(module));
    }

    public ClientConfig(Path path) {
        this.path = path;
        file=new ru.he3hauka.toolvisuals.client.storage.JsonFile(path);
        persistence=new ru.he3hauka.toolvisuals.client.storage.DeferredJson(file);
        history.reset(historyState());
    }

    public boolean enabled() {
        return active(VisualModule.NO_RENDER);
    }

    public boolean selected(RenderOption option) {
        return available(option) && hidden.contains(option);
    }

    public boolean hides(RenderOption option) {
        if (!serverPolicy.allows(FeatureCatalog.option(option))) return false;
        if (enabled() && hidden.contains(option)) return true;
        if (!active(VisualModule.NO_HURT_CAM)) return false;
        return switch (option) {
            case HURT_CAMERA -> flag(VisualSetting.CAM_HURT);
            case VIEW_BOBBING -> flag(VisualSetting.CAM_BOBBING);
            case NAUSEA, PORTAL_DISTORTION -> flag(VisualSetting.CAM_DISTORTION);
            default -> false;
        };
    }

    public int selectedCount() {
        return (int) hidden.stream().filter(this::available).count();
    }

    public boolean needsMenuBindingMigration() {
        return file.status()!=ru.he3hauka.toolvisuals.client.storage.JsonFile.Status.LOAD_FAILED&&menuBindingVersion < 1;
    }
    public boolean needsZoomBindingMigration() { return file.status()!=ru.he3hauka.toolvisuals.client.storage.JsonFile.Status.LOAD_FAILED&&menuBindingVersion < 2; }
    public void finishZoomBindingMigration() { menuBindingVersion = 2; save(); }

    public boolean lightTheme() {
        return lightTheme;
    }
    public UiLanguage language() { return language; }
    public void setLanguage(UiLanguage language) { this.language = java.util.Objects.requireNonNull(language); save(); }

    public String nickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; save(); }
    public String protectedName() { return protectedName; }
    public void setProtectedName(String name) {
        String sanitized = ru.he3hauka.toolvisuals.client.nameprotect.NameMask.sanitizeAlias(name);
        if (protectedName.equals(sanitized)) return;
        protectedName = sanitized; save();
    }

    public void setLightTheme(boolean lightTheme) {
        this.lightTheme = lightTheme;
        save();
    }

    public void finishMenuBindingMigration() {
        menuBindingVersion = 1;
        save();
    }

    public void setEnabled(boolean enabled) {
        if (!available(VisualModule.NO_RENDER)) return;
        boolean changed=this.enabled!=enabled;
        this.enabled = enabled;
        if(changed&&ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(VisualModule.NO_RENDER,"toggle","enabled="+enabled);
        save();
    }

    public void toggle(RenderOption option) {
        if (!available(option)) return;
        EnumSet<RenderOption> next = hidden.clone();
        if (!next.remove(option)) {
            next.add(option);
        }
        hidden = next;
        save();
    }

    public void setSelected(Collection<RenderOption> options, boolean selected) {
        EnumSet<RenderOption> next = hidden.clone();
        if (selected) {
            options.stream().filter(this::available).forEach(next::add);
        } else {
            options.stream().filter(this::available).forEach(next::remove);
        }
        hidden = next;
        save();
    }

    public void reset() {
        enabled = false;
        hidden = defaults();
        save();
    }

    private static ClientConfig parse(String json, boolean preset) {
            return parse(ru.he3hauka.toolvisuals.client.storage.BoundedJson.parse(json),preset);
    }

    private static ClientConfig parse(com.google.gson.JsonElement object, boolean preset) {
            if (!object.isJsonObject()) throw new IllegalArgumentException("Expected configuration object");
            if (preset && !object.getAsJsonObject().has("modules") && !object.getAsJsonObject().has("values")
                    && !object.getAsJsonObject().has("hidden")) throw new IllegalArgumentException("Not a ToolVisuals preset");
            ConfigSnapshot saved = ConfigCodec.decode(object);
            if (saved == null) {
                throw new IllegalArgumentException("Empty configuration");
            }
            ClientConfig parsed = new ClientConfig(Path.of("unused-toolvisuals-preset.json"));
            parsed.readSaved(saved);
            return parsed;
    }

    private void readSaved(ConfigSnapshot saved) {
            EnumSet<RenderOption> loaded = defaults();
            if (saved.hidden != null) {
                loaded.clear();
                for (RenderOption option : RenderOption.values()) {
                    if (saved.hidden.contains(option.id)) {
                        loaded.add(option);
                    }
                }
            }
            hidden = loaded;
            enabled = saved.noRenderEnabled;
            menuBindingVersion = Math.max(0, saved.menuBindingVersion);
            lightTheme = saved.lightTheme;
            customTitleMenu=saved.customTitleMenu;
            chatSize=saved.chatSize==null?ChatSize.DEFAULT:saved.chatSize;
            hudEditorToolbar=saved.hudEditorToolbar==null?new HudPosition(50,98):new HudPosition(saved.hudEditorToolbar.x,saved.hudEditorToolbar.y);
            language = UiLanguage.fromId(saved.uiLanguage);
            nickname = saved.nickname == null ? "" : saved.nickname;
            protectedName = ru.he3hauka.toolvisuals.client.nameprotect.NameMask.sanitizeAlias(saved.protectedName);
            pvpBlockedCommands = saved.pvpBlockedCommands == null ? ru.he3hauka.toolvisuals.client.pvp.CombatCommands.DEFAULTS
                    : ru.he3hauka.toolvisuals.client.pvp.CombatCommands.normalize(saved.pvpBlockedCommands);
            var loadedModules = EnumSet.noneOf(VisualModule.class);
            if (saved.modules != null) for (VisualModule module : VisualModule.values()) {
                if (module != VisualModule.NO_RENDER && saved.modules.contains(module.id)) loadedModules.add(module);
            }
            var loadedValues = new EnumMap<VisualSetting, Double>(VisualSetting.class);
            if (saved.values != null) for (VisualSetting setting : VisualSetting.values()) {
                Double value = saved.values.get(setting.id);
                if (value != null) loadedValues.put(setting, setting.sanitize(value));
            }
            modules = loadedModules;
            var loadedFavorites = EnumSet.noneOf(VisualModule.class);
            if (saved.favorites != null) for (var module : VisualModule.values())
                if (saved.favorites.contains(module.id)) loadedFavorites.add(module);
            favorites = Set.copyOf(loadedFavorites);
             
            values = Map.copyOf(loadedValues);
            var loadedColors=new EnumMap<VisualSetting,ColorStyle>(VisualSetting.class);
            if(saved.colors!=null)for(var setting:VisualSetting.values())if(ColorSettings.contains(setting)&&saved.colors.containsKey(setting.id))loadedColors.put(setting,ColorStyle.safe(saved.colors.get(setting.id),ColorSettings.legacy(this,setting)));
            colors=Map.copyOf(loadedColors);
            accessories=ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryConfigCodec.decode(saved.cosmeticAppearance);
            crosshairDesign=saved.crosshairDesign==null?CrosshairDesign.cross():new CrosshairDesign(saved.crosshairDesign);
            var positions = new EnumMap<HudElement, HudPosition>(HudElement.class);
            if (saved.hudPositions != null) for (var element : HudElement.values()) {
                var position = saved.hudPositions.get(element.id);
                if (position != null) positions.put(element, new HudPosition(position.x, position.y));
            }
            hudPositions = Map.copyOf(positions);
            var scales=new EnumMap<HudElement,Double>(HudElement.class);
            if(saved.hudScales!=null)for(var element:HudElement.values()){
                Double scale=saved.hudScales.get(element.id);
                if(scale!=null&&HudScale.sanitize(scale)!=1)scales.put(element,HudScale.sanitize(scale));
            }
            hudScales=Map.copyOf(scales);
    }

    public void load() {
        persistence.cancelAndAwait();
        try {
            ClientConfig parsed = file.read(json->parse(json,false));
            if(parsed==null)return;
            applyVisuals(parsed);
            menuBindingVersion = parsed.menuBindingVersion; language = parsed.language; nickname = parsed.nickname;
            hudEditorToolbar=parsed.hudEditorToolbar;
            customTitleMenu=parsed.customTitleMenu;
            history.reset(historyState());
            revision++;
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Cannot read {}; original file preserved and ordinary saving blocked", path, exception);
        }
    }

    private void applyVisuals(ClientConfig source) {
        enabled = source.enabled; hidden = source.hidden; modules = source.modules; values = source.values;
        chatSize=source.chatSize;
        hudPositions = source.hudPositions;hudScales=source.hudScales; protectedName = source.protectedName; lightTheme = source.lightTheme;
        pvpBlockedCommands = source.pvpBlockedCommands;
        favorites = source.favorites;
        colors = source.colors; crosshairDesign = source.crosshairDesign;accessories=source.accessories;
    }

     
    public String presetJson() {return ConfigTransferRules.preset(snapshot());}
    public static String normalizePreset(String json) { return parse(json, true).presetJson(); }
    private void applyPresetVisuals(ClientConfig parsed) {
        var retained=modules.clone();var retainedValues=values;var retainedChatSize=chatSize;applyVisuals(parsed);chatSize=retainedChatSize;var next=modules.clone();
        for(var module:ConfigTransferRules.PERSONAL_MODULES)if(retained.contains(module))next.add(module);else next.remove(module);modules=next;
        var settings=new EnumMap<VisualSetting,Double>(VisualSetting.class);settings.putAll(values);
        for(var setting:VisualSetting.values())if(ConfigTransferRules.PERSONAL_MODULES.contains(setting.module)){if(retainedValues.containsKey(setting))settings.put(setting,retainedValues.get(setting));else settings.remove(setting);}
        values=Map.copyOf(settings);
    }
    public void applyPreset(String json) { ClientConfig parsed = parse(json, true); applyPresetVisuals(parsed); save(); }
    public void resetVisuals() { applyPresetVisuals(new ClientConfig(path)); save(); }

    private ConfigSnapshot snapshot() {
        ConfigSnapshot saved = new ConfigSnapshot();
        saved.noRenderEnabled = enabled; saved.menuBindingVersion = menuBindingVersion;
        saved.hudEditorToolbar=hudEditorToolbar;
        saved.lightTheme = lightTheme; saved.uiLanguage = language.id; saved.nickname = nickname; saved.protectedName = protectedName;
        saved.customTitleMenu=customTitleMenu;
        saved.chatSize=chatSize;
        saved.hidden = new LinkedHashSet<>(); saved.modules = new LinkedHashSet<>(); saved.moduleDefaultsVersion = ConfigMigrations.VERSION;
        saved.customHandVersion=ConfigMigrations.CUSTOM_HAND_VERSION;
        for (VisualModule module : modules) saved.modules.add(module.id);
        saved.values = new LinkedHashMap<>(); saved.hudPositions = new LinkedHashMap<>();
        saved.hudScales=new LinkedHashMap<>();hudScales.forEach((element,scale)->saved.hudScales.put(element.id,scale));
        hudPositions.forEach((element, position) -> saved.hudPositions.put(element.id, position));
        values.forEach((setting, value) -> saved.values.put(setting.id, value));
        saved.pvpBlockedCommands = pvpBlockedCommands;
        saved.favorites = new LinkedHashSet<>();
        saved.colors = new LinkedHashMap<>(); colors.forEach((setting,color)->saved.colors.put(setting.id,color));
        saved.crosshairDesign = crosshairDesign.rows;
        saved.cosmeticAppearance=ru.he3hauka.toolvisuals.client.cosmetic.accessory.AccessoryConfigCodec.encode(accessories);
        for (var module : VisualModule.values()) if (favorite(module)) saved.favorites.add(module.id);
        for (RenderOption option : hidden) saved.hidden.add(option.id);
        return saved;
    }

    public void save(){save(null);}
    private void save(String historyGroup) {
        revision++;
        var saved=snapshot();
        if(!restoringHistory)history.recordDeferred(()->ConfigTransferRules.history(saved),System.nanoTime(),historyGroup);
        try {
            if(historyGroup==null)persistence.write(()->ConfigCodec.encode(saved));
            else persistence.request(()->ConfigCodec.encode(saved));
        }catch(IOException error){LOGGER.warn("Visual changes are not saved; original configuration preserved",error);}
    }


    public void undo(){restoreHistory(history.undo());}
    public void redo(){restoreHistory(history.redo());}
    private void restoreHistory(String state) {
        if(state==null)return;
        var parsed=parse(state,false);applyVisuals(parsed);
        language=parsed.language;
        hudEditorToolbar=parsed.hudEditorToolbar;customTitleMenu=parsed.customTitleMenu;
        restoringHistory=true;try{save();}finally{restoringHistory=false;}
    }
    private String historyState() {return ConfigTransferRules.history(snapshot());}

    private static EnumSet<RenderOption> defaults() {
        EnumSet<RenderOption> result = EnumSet.noneOf(RenderOption.class);
        for (RenderOption option : RenderOption.values()) {
            if (option.hiddenByDefault) {
                result.add(option);
            }
        }
        return result;
    }

}
