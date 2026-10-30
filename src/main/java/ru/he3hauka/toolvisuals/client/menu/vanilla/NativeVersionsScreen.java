package ru.he3hauka.toolvisuals.client.menu.vanilla;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.connection.ConnectionVersions;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class NativeVersionsScreen extends NativeMenuScreen {
    private final List<Button> rows=new ArrayList<>();
    private List<ConnectionVersions.Choice> choices=List.of();
    private int first,visible;
    private String query="";
    public NativeVersionsScreen(Screen parent){super(parent,UiI18n.tr("toolvisuals.main.versions"));}
    @Override protected void init(){
        rows.clear();choices=ConnectionVersions.choices();visible=Math.max(1,(height-118)/24)*2;
        var search=field(UiI18n.tr("toolvisuals.ui.search"),left(),52,contentWidth());search.setMaxLength(64);search.setValue(query);
        search.setResponder(value->{query=value;first=0;rebuildRows();});back();rebuildRows();setInitialFocus(search);
    }
    private List<ConnectionVersions.Choice> filtered(){String needle=query.strip().toLowerCase(Locale.ROOT);return choices.stream().filter(choice->choice.label.toLowerCase(Locale.ROOT).contains(needle)).toList();}
    private void rebuildRows(){
        rows.forEach(this::removeWidget);rows.clear();var filtered=filtered();first=Math.clamp(first,0,Math.max(0,((filtered.size()-visible+1)/2)*2));
        int half=(contentWidth()-4)/2;
        for(int i=first;i<Math.min(filtered.size(),first+visible);i++){
            var choice=filtered.get(i);int index=i-first;
            var button=button(Component.literal((ConnectionVersions.selected(choice)?"✓ ":"")+choice.label),left()+index%2*(half+4),78+index/2*24,half,()->{ConnectionVersions.select(choice);onClose();});
            button.setTooltip(Tooltip.create(Component.literal(choice.label)));rows.add(button);
        }
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(y>=78&&y<height-40){first+=(int)-Math.signum(vertical)*2;rebuildRows();return true;}return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override protected void labels(GuiGraphicsExtractor graphics){graphics.textRenderer().accept(TextAlignment.CENTER,width/2,36,UiI18n.tr("toolvisuals.connection.version_value",ConnectionVersions.label()));}
}
