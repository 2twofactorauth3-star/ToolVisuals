package ru.he3hauka.toolvisuals.client.utility.screenshots.ui;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.utility.preview.PreviewImages;
import ru.he3hauka.toolvisuals.client.utility.screenshots.ScreenshotFiles;
import ru.he3hauka.toolvisuals.client.utility.ui.UtilityScreen;

public final class ScreenshotManagerScreen extends UtilityScreen {
    private List<Path> files=List.of();
    private Path selected;
    private PreviewImages previews;
    public ScreenshotManagerScreen(Screen parent){super(parent,"screenshots");}
    private Path directory(){return minecraft.gameDirectory.toPath().resolve("screenshots");}
    @Override protected void init() {
        begin();if(previews==null)previews=new PreviewImages();
        try{files=ScreenshotFiles.list(directory());}catch(Exception error){ClientErrors.report("List screenshots",error);status=ui("failed").getString();}
        if(selected==null&&!files.isEmpty())selected=files.getFirst();
        if(selected!=null){var file=selected;previews.request(file.toString(),()->Files.newInputStream(file));}
        button(24,66,176,28,ui("folder"),null,()->attempt(()->{try{Files.createDirectories(directory());Blaze3D.openPath(directory());}catch(Exception error){throw new IllegalStateException(error);}}),null);
        button(212,66,176,28,ui("open_image"),null,()->{if(selected!=null)Blaze3D.openPath(selected);},null).active=selected!=null;
        button(400,66,196,28,ui("copy_image"),null,this::copy,null).active=selected!=null;
        refresh();
    }
    private void refresh() {
        clearRows();pages(files.size(),this::refresh);
        for(int i=0;i<7&&page*7+i<files.size();i++){var file=files.get(page*7+i);row(i,24,264,Component.literal(file.getFileName().toString()),()->{selected=file;previews.request(file.toString(),()->Files.newInputStream(file));rebuildWidgets();});}
        if(files.isEmpty())status=ui("no_screenshots").getString();
    }
    private void copy() {
        if(selected==null)return;var file=selected;status=ui("loading").getString();
        CompletableFuture.runAsync(()->{
            try {
                var image=PreviewImages.read(()->Files.newInputStream(file),16384);if(image==null)throw new IllegalStateException("Not an image");
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable(){
                    @Override public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[]{DataFlavor.imageFlavor};}
                    @Override public boolean isDataFlavorSupported(DataFlavor flavor){return DataFlavor.imageFlavor.equals(flavor);}
                    @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {if(!isDataFlavorSupported(flavor))throw new UnsupportedFlavorException(flavor);return image;}
                },null);minecraft.execute(()->status=ui("copied").getString());
            }catch(Exception error){ClientErrors.report("Copy screenshot",error);minecraft.execute(()->status=ui("failed").getString());}
        });
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my) {
        UiPaint.rounded(g,px+304,py+114,292,248,8,palette.field);
        if(selected!=null){previews.draw(selected.toString(),g,px+312,py+122,276,232);label(g,selected.getFileName().toString(),304,366,292,.7f,palette.muted);}footer(g);
    }
    @Override public void removed(){super.removed();if(previews!=null){previews.close();previews=null;}}
}
