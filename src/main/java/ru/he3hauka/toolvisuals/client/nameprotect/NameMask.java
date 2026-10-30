package ru.he3hauka.toolvisuals.client.nameprotect;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public final class NameMask {
    public final String name, alias;
    private final Pattern pattern;
    private record Glyph(int codepoint, Style style, int offset) {}
    public NameMask(String name, String alias) {
        this.name = name; this.alias = sanitizeAlias(alias);
         
        pattern = Pattern.compile("(?<![A-Za-z0-9_])" + Pattern.quote(this.alias) + "(?![A-Za-z0-9_])"
                + "|(?<name>(?i:(?<![A-Za-z0-9_])" + Pattern.quote(name) + "(?![A-Za-z0-9_])))");
    }
    public static String sanitizeAlias(String value) {
        if (value == null) return "ToolVisuals";
        var clean = new StringBuilder();
        value.codePoints().filter(cp -> !Character.isISOControl(cp) && cp != 0x00a7).forEach(cp -> {
            if (clean.length() + Character.charCount(cp) <= 32) clean.appendCodePoint(cp);
        });
        String result = clean.toString().strip();
        return result.isEmpty() ? "ToolVisuals" : result;
    }
    public String apply(String text) {
        if (text == null || text.isEmpty() || name.isEmpty() || name.equals(alias)) return text;
        var matcher = pattern.matcher(text); StringBuilder result = null; int last = 0;
        while (matcher.find()) if (matcher.group("name") != null) {
            if (result == null) result = new StringBuilder(text.length());
            result.append(text,last,matcher.start()).append(alias); last = matcher.end();
        }
        return result == null ? text : result.append(text,last,text.length()).toString();
    }
    public FormattedCharSequence apply(FormattedCharSequence source) {
        var glyphs = new ArrayList<Glyph>(); var text = new StringBuilder();
        source.accept((index,style,cp) -> { glyphs.add(new Glyph(cp,style,text.length())); text.appendCodePoint(cp); return true; });
        List<Glyph> masked = replace(text.toString(),glyphs);
        if (masked == glyphs) return source;
        return sink -> {
            int index = 0;
            for (var glyph : masked) {
                if (!sink.accept(index,glyph.style,glyph.codepoint)) return false;
                index += Character.charCount(glyph.codepoint);
            }
            return true;
        };
    }
    public FormattedText apply(FormattedText source) {
        var glyphs = new ArrayList<Glyph>(); var text = new StringBuilder();
        StringDecomposer.iterateFormatted(source,Style.EMPTY,(index,style,cp) -> {
            glyphs.add(new Glyph(cp,style,text.length())); text.appendCodePoint(cp); return true;
        });
        List<Glyph> masked = replace(text.toString(),glyphs);
        if (masked == glyphs) return source;
        var parts = new ArrayList<FormattedText>(); var run = new StringBuilder(); Style style = null;
        for (var glyph : masked) {
            if (!glyph.style.equals(style) && !run.isEmpty()) { parts.add(FormattedText.of(run.toString(),style)); run.setLength(0); }
            style = glyph.style; run.appendCodePoint(glyph.codepoint);
        }
        if (!run.isEmpty()) parts.add(FormattedText.of(run.toString(),style));
        return FormattedText.composite(parts);
    }
    private List<Glyph> replace(String text,List<Glyph> glyphs) {
        if (name.isEmpty() || name.equals(alias)) return glyphs;
        var matcher = pattern.matcher(text); ArrayList<Glyph> result = null; int cursor = 0;
        while (matcher.find()) if (matcher.group("name") != null) {
            if (result == null) result = new ArrayList<>(glyphs.size());
            while (cursor < glyphs.size() && glyphs.get(cursor).offset < matcher.start()) result.add(glyphs.get(cursor++));
            Style style = glyphs.get(cursor).style;
            for (int cp : alias.codePoints().toArray()) result.add(new Glyph(cp,style,0));
            while (cursor < glyphs.size() && glyphs.get(cursor).offset < matcher.end()) cursor++;
        }
        if (result == null) return glyphs;
        result.addAll(glyphs.subList(cursor,glyphs.size())); return result;
    }
}
