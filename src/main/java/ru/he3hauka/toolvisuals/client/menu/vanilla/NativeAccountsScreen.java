package ru.he3hauka.toolvisuals.client.menu.vanilla;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.connection.AccountStore;
import ru.he3hauka.toolvisuals.client.connection.ConnectionIdentity;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.storage.JsonFile;

public final class NativeAccountsScreen extends NativeMenuScreen {
    private record Account(String name,boolean launcher,boolean favorite){}
    @FunctionalInterface private interface Action {void run() throws IOException;}
    private final AccountStore store=ToolVisualsClient.ACCOUNTS;
    private final List<Button> rows=new ArrayList<>();
    private EditBox search,nickname;
    private Button use,favorite,delete,add;
    private String selected,query="",draft="";
    private Component status=Component.empty();
    private int first,visible;
    public NativeAccountsScreen(Screen parent){super(parent,tr("title"));}
    private static Component tr(String key,Object... arguments){return UiI18n.tr("toolvisuals.accounts."+key,arguments);}
    @Override protected void init(){
        rows.clear();visible=Math.max(1,(height-182)/24);if(selected==null)selected=minecraft.getUser().getName();
        if(store.status()==JsonFile.Status.LOAD_FAILED)status=tr("io_error");
        search=field(tr("search"),left(),52,contentWidth());search.setMaxLength(32);search.setValue(query);search.setResponder(value->{query=value;first=0;rebuildRows();});
        int third=(contentWidth()-8)/3;
        nickname=field(tr("new_name"),left(),height-90,contentWidth()-120);nickname.setMaxLength(ConnectionIdentity.MAX_NAME_LENGTH);nickname.setValue(draft);nickname.setResponder(value->{draft=value;updateActions();});
        add=button(tr("add"),left()+contentWidth()-116,height-90,56,this::addProfile);
        button(tr("random"),left()+contentWidth()-56,height-90,56,()->{nickname.setValue(ConnectionIdentity.randomName());addProfile();});
        use=button(tr("use"),left(),height-64,third,()->activate(selected));
        favorite=button(tr("favorite"),left()+third+4,height-64,third,()->perform(()->{var account=selected();if(account!=null)store.favorite(account.name,!account.favorite);}));
        delete=button(tr("delete"),left()+2*(third+4),height-64,contentWidth()-2*(third+4),()->perform(()->{
            var account=selected();if(account==null||account.launcher)return;
            store.remove(account.name);if(minecraft.getUser().getName().equals(account.name))ConnectionIdentity.restore(minecraft);
            status=tr("removed",account.name);selected=minecraft.getUser().getName();
        }));
        back();rebuildRows();setInitialFocus(search);
    }
    private List<Account> accounts(){
        var result=new ArrayList<Account>();String launcher=ConnectionIdentity.launcherProfile(minecraft).name();result.add(new Account(launcher,true,false));
        for(var profile:store.profiles())if(!profile.name.equals(launcher))result.add(new Account(profile.name,false,profile.favorite));
        return result;
    }
    private Account selected(){return accounts().stream().filter(account->account.name.equals(selected)).findFirst().orElse(null);}
    private void activate(String name){perform(()->{ConnectionIdentity.apply(minecraft,name);selected=name;status=tr("switched",name);});}
    private void perform(Action action){
        try{action.run();}catch(IOException error){status=tr("io_error");}catch(IllegalStateException error){status=tr("limit");}
        rebuildRows();
    }
    private void addProfile(){
        if(!ConnectionIdentity.valid(nickname.getValue()))return;
        perform(()->{String name=nickname.getValue();boolean added=store.add(name);selected=name;status=tr(added?"added":"exists",name);nickname.setValue("");search.setValue("");first=0;});
    }
    private void updateActions(){
        if(use==null||nickname==null||add==null)return;
        var account=selected();use.active=account!=null&&!minecraft.getUser().getName().equals(account.name);
        favorite.active=delete.active=account!=null&&!account.launcher;add.active=ConnectionIdentity.valid(nickname.getValue())&&store.profiles().size()<AccountStore.LIMIT;
    }
    private void rebuildRows(){
        rows.forEach(this::removeWidget);rows.clear();String needle=query.strip().toLowerCase(Locale.ROOT);
        var accounts=accounts().stream().filter(account->account.name.toLowerCase(Locale.ROOT).contains(needle)).toList();
        first=Math.clamp(first,0,Math.max(0,accounts.size()-visible));
        for(int i=first;i<Math.min(accounts.size(),first+visible);i++){
            var account=accounts.get(i);int y=78+(i-first)*24;
            String label=(account.name.equals(selected)?"✓ ":"")+(account.favorite?"★ ":"")+account.name;
            rows.add(button(Component.literal(label),left(),y,contentWidth()-24,()->{selected=account.name;rebuildRows();}));
            rows.add(button(Component.literal("→"),left()+contentWidth()-20,y,20,()->activate(account.name)));
        }
        updateActions();
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(y>=78&&y<height-98){first+=(int)-Math.signum(vertical);rebuildRows();return true;}return super.mouseScrolled(x,y,horizontal,vertical);
    }
    @Override protected void labels(GuiGraphicsExtractor graphics){
        graphics.textRenderer().accept(TextAlignment.CENTER,width/2,36,tr("active",minecraft.getUser().getName()));
        graphics.textRenderer().accept(TextAlignment.CENTER,width/2,height-40,status);
    }
}
