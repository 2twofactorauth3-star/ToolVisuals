package ru.he3hauka.toolvisuals.client.cape.ui;

import java.util.EnumMap;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.cape.CapePresets;
import ru.he3hauka.toolvisuals.client.cape.CapeTexture;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

final class CapePresetScreen extends VisualEditorScreen {
    private record Preview(CapeDesign design,CapeTexture texture) {}
    private final CapeEditorScreen editor;
    private final int primary,secondary;
    private final EnumMap<CapePresets.Kind,Preview> previews=new EnumMap<>(CapePresets.Kind.class);
    private CapePresets.Kind selected;

    CapePresetScreen(CapeEditorScreen editor,CapePresets.Kind selected,int primary,int secondary) {
        super(editor,CapeEditorScreen.text("presets_title"));this.editor=editor;this.selected=selected;this.primary=primary;this.secondary=secondary;
    }
    @Override protected void init() {
        frame(680,430);
        for(var kind:CapePresets.Kind.values()) {
            previews.computeIfAbsent(kind,key->new Preview(CapePresets.create(key,primary,secondary),new CapeTexture(true)));
            int x=20+kind.ordinal()%5*130,y=66+kind.ordinal()/5*148;
            var card=addWidget(new VisualsButton(px+x,py+y,120,138,name(kind),VisualsButton.Kind.CHIP,null,Component.empty(),()->selected=kind,()->selected==kind) {
                @Override protected void extractContents(GuiGraphicsExtractor g,int mx,int my,float partial) {
                    var palette=CapePresetScreen.this.palette;
                    isHovered=UiPaint.hovered(g,this,mx,my);
                    int fill=selected==kind?palette.selected:isHoveredOrFocused()?palette.hover:palette.field;
                    UiPaint.rounded(g,getX(),getY(),getWidth(),getHeight(),8,fill);
                    var preview=previews.get(kind);preview.texture.update(preview.design);
                    if(preview.texture.ready())g.blit(RenderPipelines.GUI_TEXTURED,preview.texture.id,getX()+35,getY()+12,2,2,50,80,20,32,128,64,0xffffffff);
                    UiPaint.centered(g,UiPaint.fit(g,getMessage(),110,.77f),getX()+5,getY()+98,110,18,.77f,palette.text,1);
                    if(kind==CapePresets.DEFAULT)UiPaint.centered(g,CapeEditorScreen.text("preset_default").getString(),getX()+4,getY()+117,112,14,.6f,palette.muted,1);
                    if(selected==kind)UiPaint.icon(g,UiPaint.Icon.CHECK,getX()+100,getY()+7,12,palette.accentText,1);
                }
            });controls.add(card);
        }
        button(360,384,144,28,CapeEditorScreen.text("preset_apply"),UiPaint.Icon.CHECK,()->{editor.applyPreset(selected);minecraft.gui.setScreen(editor);},null);
        button(514,384,146,28,CapeEditorScreen.text("return_editor"),UiPaint.Icon.BACK,this::onClose,null);
    }
    private static Component name(CapePresets.Kind kind) {return CapeEditorScreen.text("preset_"+kind.name().toLowerCase(Locale.ROOT));}
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my) {
        UiPaint.baseline(g,UiPaint.fit(g,CapeEditorScreen.text("preset_hint"),326,.7f),px+20,UiPaint.textBaseline(g,py+380,36,.7f),.7f,palette.muted,1);
    }
    @Override public void removed() {previews.values().forEach(preview->preview.texture.close());previews.clear();super.removed();}
}
