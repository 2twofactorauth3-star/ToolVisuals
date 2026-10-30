package ru.he3hauka.toolvisuals.mixin.chat;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.utility.chat.*;
import ru.he3hauka.toolvisuals.client.utility.chat.ui.ChatToolsScreen;

@Mixin(ChatScreen.class)
public abstract class ChatTabsScreenMixin extends Screen {
    @Shadow protected EditBox input;
    @Shadow protected String initial;
    @Unique private final java.util.List<Button> toolvisuals$tabs=new java.util.ArrayList<>();
    protected ChatTabsScreenMixin(){super(Component.empty());}
    @Unique private void toolvisuals$openChatTools(){initial=input.getValue();minecraft.gui.setScreen(new ChatToolsScreen(this,false));}
    @Inject(method="init",at=@At("TAIL"))
    private void toolvisuals$tabs(CallbackInfo ci) {
        if(!ToolVisualsClient.CONFIG.active(VisualModule.CHAT_TABS))return;
        toolvisuals$tabs.clear();
        int full=Math.min(540,width-8),unit=(full-72)/4,index=0;
        for(var tab:ChatRules.Tab.values()) {
            var button=addRenderableWidget(Button.builder(UiI18n.tr("toolvisuals.utility.chat_tab_"+tab.name().toLowerCase(java.util.Locale.ROOT)),pressed->{ChatTabs.select(tab);for(int i=0;i<toolvisuals$tabs.size();i++)toolvisuals$tabs.get(i).active=i!=tab.ordinal();setFocused(input);}).bounds(4+index++*unit,4,unit-4,18).build());
            toolvisuals$tabs.add(button);
            button.active=ChatTabs.selected()!=tab;
        }
        int x=4+unit*4;
        if(ToolVisualsClient.CONFIG.flag(VisualSetting.CHAT_COPY)) {
            var copy=addRenderableWidget(Button.builder(UiI18n.tr("toolvisuals.utility.copy_short"),pressed->toolvisuals$openChatTools()).bounds(x,4,38,18).build());
            copy.setTooltip(Tooltip.create(UiI18n.tr("toolvisuals.utility.copy_message_hint")));
        }
        addRenderableWidget(Button.builder(Component.literal("…"),pressed->toolvisuals$openChatTools()).bounds(x+42,4,26,18).build());
    }
}
