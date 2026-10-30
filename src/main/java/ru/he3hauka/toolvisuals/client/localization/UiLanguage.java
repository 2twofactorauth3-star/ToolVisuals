package ru.he3hauka.toolvisuals.client.localization;

import java.util.Locale;

public enum UiLanguage {
    ENGLISH("en_us","EN",Locale.ENGLISH),
    RUSSIAN("ru_ru","RU",Locale.forLanguageTag("ru"));

    public final String id, badge;
    public final Locale locale;
    UiLanguage(String id,String badge,Locale locale) { this.id=id; this.badge=badge; this.locale=locale; }
    public UiLanguage next() { return this == ENGLISH ? RUSSIAN : ENGLISH; }
    public static UiLanguage fromId(String id) {
        for (var language : values()) if (language.id.equalsIgnoreCase(id)) return language;
        return ENGLISH;
    }
}
