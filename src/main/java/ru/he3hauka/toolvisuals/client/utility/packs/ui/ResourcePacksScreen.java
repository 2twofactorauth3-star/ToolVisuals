package ru.he3hauka.toolvisuals.client.utility.packs.ui;

import java.util.*;

import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.utility.packs.ResourcePacks;
import ru.he3hauka.toolvisuals.client.utility.preview.PreviewImages;
import ru.he3hauka.toolvisuals.client.utility.ui.UtilityScreen;

public final class ResourcePacksScreen extends UtilityScreen {
    private final Set<String> draft=new LinkedHashSet<>();
    private List<Pack> packs=List.of();
    private PreviewImages previews;
    private boolean initialized,favorites;
    public ResourcePacksScreen(Screen parent){super(parent,"packs");}
    @Override protected void init() {
        begin();preferencesStatus();if(previews==null)previews=new PreviewImages();
        var repository=minecraft.getResourcePackRepository();repository.reload();
        if(!initialized){draft.addAll(repository.getSelectedIds());initialized=true;}
        button(24,66,136,28,ui("folder"),null,()->Blaze3D.openPath(minecraft.getResourcePackDirectory()),null);
        button(168,66,136,28,ui("pack_sets"),null,()->minecraft.gui.setScreen(new PackSetsScreen(this)),null);
        button(312,66,136,28,ui("favorites"),null,()->{favorites=!favorites;page=0;refresh();},()->favorites);
        button(456,66,140,28,ui("apply"),null,()->{ResourcePacks.apply(minecraft,draft);status=ui("done").getString();},null);
        refresh();
    }
    void useSet(Collection<String> ids){draft.clear();draft.addAll(ids);status=ui("set_selected").getString();}
    Collection<String> selected(){return List.copyOf(draft);}
    private void toggle(Pack pack) {
        if(draft.contains(pack.getId())){draft.remove(pack.getId());refresh();return;}
        if(!pack.getCompatibility().isCompatible()) {
            minecraft.gui.setScreen(new ConfirmScreen(accept->{if(accept)draft.add(pack.getId());minecraft.gui.setScreen(this);},pack.getTitle(),Component.translatable("pack.incompatible.confirm.new")));return;
        }
        draft.add(pack.getId());refresh();
    }
    private void refresh() {
        clearRows();var preferences=ToolVisualsClient.UTILITIES;
        packs=minecraft.getResourcePackRepository().getAvailablePacks().stream().filter(pack->!favorites||preferences.favorite(pack.getId())).sorted(Comparator.<Pack,Boolean>comparing(pack->!preferences.favorite(pack.getId())).thenComparing(pack->pack.getTitle().getString())).toList();
        pages(packs.size(),this::refresh);
        for(int i=0;i<7&&page*7+i<packs.size();i++) {
            var pack=packs.get(page*7+i);previews.request(pack.getId(),()->ResourcePacks.icon(pack));
            var row=button(66,114+i*40,444,36,pack.getTitle(),draft.contains(pack.getId())?UiPaint.Icon.CHECK:null,()->toggle(pack),()->draft.contains(pack.getId()));listControls.add(row);row.active=!pack.isRequired();row.setTooltip(net.minecraft.client.gui.components.Tooltip.create(pack.getDescription()));
            var favorite=button(520,114+i*40,36,36,Component.empty(),preferences.favorite(pack.getId())?UiPaint.Icon.STAR_FILLED:UiPaint.Icon.STAR,()->persist(()->preferences.toggleFavorite(pack.getId()),this::refresh),null);listControls.add(favorite);
            var order=button(564,114+i*40,32,36,Component.literal("↑"),null,()->raise(pack.getId()),null);listControls.add(order);order.active=draft.contains(pack.getId())&&!pack.isFixedPosition();
        }
    }
    private void raise(String id){var ids=new ArrayList<>(draft);int index=ids.indexOf(id);if(index>=0&&index+1<ids.size()){Collections.swap(ids,index,index+1);draft.clear();draft.addAll(ids);status=ui("priority_up").getString();}}
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my) {
        for(int i=0;i<7&&page*7+i<packs.size();i++){int y=py+116+i*40;UiPaint.rounded(g,px+24,y,32,32,5,palette.field);previews.draw(packs.get(page*7+i).getId(),g,px+24,y,32,32);}footer(g);
    }
    @Override public void removed(){super.removed();if(previews!=null){previews.close();previews=null;}}
}
