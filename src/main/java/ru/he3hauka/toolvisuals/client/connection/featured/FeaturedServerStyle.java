package ru.he3hauka.toolvisuals.client.connection.featured;

import java.util.EnumMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.localization.UiLanguage;
import ru.he3hauka.toolvisuals.client.menu.TitleBrand;

public final class FeaturedServerStyle {
    private record Appearance(Component title,Component promotion){}
    private static final EnumMap<UiLanguage,Appearance> APPEARANCES=new EnumMap<>(UiLanguage.class);

    static {
        for(var language:UiLanguage.values()){
            String label=UiI18n.text(language,"toolvisuals.server.best");
            var title=Component.empty();
            int count=label.codePointCount(0,label.length()),position=0;
            for(int offset=0;offset<label.length();position++){
                int codePoint=label.codePointAt(offset);offset+=Character.charCount(codePoint);
                int color=TitleBrand.color(position/(double)Math.max(1,count-1),0,false)&0xffffff;
                title.append(Component.literal(new String(Character.toChars(codePoint))).withColor(color).withStyle(style->style.withBold(true)));
            }
            var promotion=Component.empty()
                    .append(Component.literal("F").withColor(0xb59aff).withStyle(style->style.withBold(true).withObfuscated(true)))
                    .append(word(" "+UiI18n.text(language,"toolvisuals.server.promo.intro")+" ",0xf0edff))
                    .append(word(UiI18n.text(language,"toolvisuals.server.promo.wipe"),0xf187d2))
                    .append(word(" "+UiI18n.text(language,"toolvisuals.server.promo.code")+" ",0xe5e2f1))
                    .append(word("/TOOLVISUALS",0xffc55c))
                    .append(word(" ФРИ СЕТКА",0xffffff));
            APPEARANCES.put(language,new Appearance(title,promotion));
        }
    }

    private FeaturedServerStyle(){}
    private static MutableComponent word(String text,int color){return Component.literal(text).withColor(color).withStyle(style->style.withBold(true).withObfuscated(false));}
    public static Component title(UiLanguage language){return APPEARANCES.get(language).title;}
    public static Component promotion(UiLanguage language){return APPEARANCES.get(language).promotion;}
}
