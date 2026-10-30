package ru.he3hauka.toolvisuals.client.utility.chat;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ChatRules {
    public enum Tab {ALL,PRIVATE,SYSTEM,CUSTOM}
    private static final Pattern PRIVATE=Pattern.compile("(?iu)(whispers? (?:to you|to)|(?:you|to|from) [A-Za-z0-9_]+:|(?:вам|тебе|шепчет|личное сообщение)|\\[(?:лс|pm|msg|tell)\\]|(?:[A-Za-z0-9_]+|я|вы)\\s*(?:->|→|»)[ ]*(?:[A-Za-z0-9_]+|я|вы))");
    private ChatRules(){}
    public static boolean privateMessage(String text){return PRIVATE.matcher(text).find();}
    public static boolean visible(Tab tab,String text,boolean system,List<String> filters,boolean hidePrivate) {
        boolean personal=privateMessage(text);if(hidePrivate&&personal)return false;
        return switch(tab){case ALL->true;case PRIVATE->personal;case SYSTEM->system&&!personal;case CUSTOM->filters.stream().anyMatch(term->text.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT)));};
    }
}
