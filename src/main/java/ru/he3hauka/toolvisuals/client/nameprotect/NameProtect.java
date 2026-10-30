package ru.he3hauka.toolvisuals.client.nameprotect;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.optimization.cache.BoundedCache;
import ru.he3hauka.toolvisuals.client.optimization.text.StableText;

public final class NameProtect {
    private static final ThreadLocal<Integer> RAW = ThreadLocal.withInitial(() -> 0);
    private static volatile NameMask mask;
    private static String chatState = "";
    private record SequenceKey(NameMask mask,StableText source) {}
    private static final BoundedCache<SequenceKey,FormattedCharSequence> SEQUENCES = new BoundedCache<>(512);
    private NameProtect() {}
    public static void beginRawText() { RAW.set(RAW.get()+1); }
    public static void endRawText() { RAW.set(RAW.get()-1); }
    private static NameMask mask() {
        if (RAW.get() > 0 || !ToolVisualsClient.CONFIG.active(VisualModule.NAME_PROTECT)) return null;
        var client = Minecraft.getInstance();
        if (client == null || client.getUser() == null) return null;
        String name = client.getUser().getName(), alias = ToolVisualsClient.CONFIG.protectedName();
        var current = mask;
        if (current == null || !current.name.equals(name) || !current.alias.equals(alias)) mask = current = new NameMask(name,alias);
        return current;
    }
    public static String text(String text) { if(RAW.get()>0)return text;var m = mask();return ru.he3hauka.toolvisuals.client.utility.privacy.PrivacyMode.text(m == null ? text : m.apply(text)); }
    public static FormattedText text(FormattedText text) { if(RAW.get()>0)return text;var m = mask();return ru.he3hauka.toolvisuals.client.utility.privacy.PrivacyMode.text(m == null ? text : m.apply(text)); }
    public static FormattedCharSequence text(FormattedCharSequence text) {
        if(RAW.get()>0)return text;
        text=ru.he3hauka.toolvisuals.client.utility.privacy.PrivacyMode.text(text);
        var m = mask(); if (m == null) return text;
        if (!(text instanceof StableText stable)) return m.apply(text);
        var key = new SequenceKey(m,stable); var cached = SEQUENCES.get(key);
        if (cached != null) return cached;
        var masked = m.apply(text);
        var result = masked == text ? text : new StableText(masked);
        SEQUENCES.put(key,result); return result;
    }
    public static void tick(Minecraft client) {
        var m = mask(); String state = m == null ? "" : m.name + "\n" + m.alias;
        if (!state.equals(chatState)) {
            chatState = state;
             
            client.gui.hud.getChat().rescaleChat();
        }
    }
}
