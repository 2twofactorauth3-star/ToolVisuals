package ru.he3hauka.toolvisuals.client.cape.ui;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.cape.CapeImageImport;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

final class CapeResizeScreen extends VisualEditorScreen {
    private final CapeImageImport source;
    private final Consumer<CapeDesign> apply;
    private CapeImageImport.Layout selected;

    CapeResizeScreen(Screen parent,CapeImageImport source,Consumer<CapeDesign> apply) {
        super(parent,CapeEditorScreen.text(source.requiresResize()?"resize_title":"animation_import"));this.source=source;this.apply=apply;selected=source.preferred;
    }

    @Override protected void init() {
        frame(620,354);
        int index=0;
        for(var layout:CapeImageImport.Layout.values()) {
            button(20+index++*146,136,134,32,Component.literal(layout.size()),null,()->selected=layout,()->selected==layout);
        }
        button(20,302,282,32,tr("cancel"),UiPaint.Icon.CLOSE,this::onClose,null);
        button(316,302,284,32,tr("apply"),UiPaint.Icon.CHECK,()->{
            var design=source.fit(selected);apply.accept(design);onClose();
        },null);
    }

    @Override protected void paint(GuiGraphicsExtractor graphics,int mouseX,int mouseY) {
        UiPaint.text(graphics,CapeEditorScreen.text("resize_actual",source.width,source.height),px+20,py+68,.95f,palette.text,1);
        UiPaint.text(graphics,CapeEditorScreen.text("resize_supported"),px+20,py+102,.8f,palette.muted,1);
        UiPaint.text(graphics,CapeEditorScreen.text("resize_target",selected.size()),px+20,py+190,.9f,palette.accentText,1);
        UiPaint.text(graphics,CapeEditorScreen.text("resize_hint"),px+20,py+228,.75f,palette.muted,1);
        if(source.sourceFrames>1)UiPaint.text(graphics,CapeEditorScreen.text("animation_conversion",source.sourceFrames,source.frameCount,source.fps),px+20,py+263,.75f,palette.accentText,1);
    }
}
