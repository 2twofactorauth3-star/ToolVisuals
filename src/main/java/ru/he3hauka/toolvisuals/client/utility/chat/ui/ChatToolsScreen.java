package ru.he3hauka.toolvisuals.client.utility.chat.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.nameprotect.NameProtect;
import ru.he3hauka.toolvisuals.client.utility.chat.*;
import ru.he3hauka.toolvisuals.client.utility.ui.UtilityScreen;
import ru.he3hauka.toolvisuals.mixin.chat.ChatMessagesAccessor;

public final class ChatToolsScreen extends UtilityScreen {
    public final boolean privacy;
    private EditBox input;
    private String draft;
    public ChatToolsScreen(Screen parent,boolean privacy){super(parent,privacy?"privacy_names":"chat");this.privacy=privacy;}
    @Override protected void init() {
        begin();preferencesStatus();var preferences=ToolVisualsClient.UTILITIES;
        input=field(30,66,432,draft==null?String.join(", ",privacy?preferences.hiddenNames():preferences.filters()):draft,2048,text->draft=text);
        button(484,66,112,28,ui("apply"),null,()->persist(()->{if(privacy)preferences.setHiddenNames(input.getValue());else preferences.setFilters(input.getValue());},()->{ChatTabs.refresh(minecraft);status=ui("done").getString();refresh();}),null);
        refresh();
    }
    private void refresh() {
        clearRows();if(privacy)return;
        var messages=((ChatMessagesAccessor)minecraft.gui.hud.getChat()).toolvisuals$messages().stream().filter(ChatTabs::visible).toList();pages(messages.size(),this::refresh);
        for(int i=0;i<7&&page*7+i<messages.size();i++) {
            String text=messages.get(page*7+i).content().getString();
            row(i,24,572,Component.literal(NameProtect.text(text)),()->{minecraft.keyboardHandler.setClipboard(NameProtect.text(text));status=ui("copied").getString();});
        }
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my) {
        if(privacy){label(g,ui("privacy_names_hint").getString(),24,130,572,.85f,palette.muted);label(g,ui("privacy_names_hint2").getString(),24,164,572,.8f,palette.muted);}
        footer(g);
    }
}
