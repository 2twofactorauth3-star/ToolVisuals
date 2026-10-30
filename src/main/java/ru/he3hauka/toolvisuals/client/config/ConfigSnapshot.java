package ru.he3hauka.toolvisuals.client.config;

import java.util.Map;
import java.util.Set;

import ru.he3hauka.toolvisuals.client.chat.layout.ChatSize;
import ru.he3hauka.toolvisuals.client.config.color.ColorStyle;
import ru.he3hauka.toolvisuals.client.hud.layout.HudPosition;

final class ConfigSnapshot {
    public boolean noRenderEnabled;
    public int menuBindingVersion;
    public boolean lightTheme;
    public boolean customTitleMenu;
    public ChatSize chatSize;
    public String uiLanguage;
    public String nickname;
    public String protectedName;
    public Set<String> hidden;
    public Set<String> modules;
    public int moduleDefaultsVersion;
    public int customHandVersion;
    public Map<String, Double> values;
    public Map<String, HudPosition> hudPositions;
    public Map<String, Double> hudScales;
    public HudPosition hudEditorToolbar;
    public java.util.List<String> pvpBlockedCommands;
    public Set<String> favorites;
    public Map<String,ColorStyle> colors;
    public java.util.List<String> crosshairDesign;
    public String cosmeticAppearance;
}
