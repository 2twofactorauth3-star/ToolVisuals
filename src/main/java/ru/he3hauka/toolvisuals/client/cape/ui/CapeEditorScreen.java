package ru.he3hauka.toolvisuals.client.cape.ui;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.input.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import ru.he3hauka.toolvisuals.client.*;
import ru.he3hauka.toolvisuals.client.cape.*;
import ru.he3hauka.toolvisuals.client.cape.animation.CapeAnimationDraft;
import ru.he3hauka.toolvisuals.client.cosmetic.preview.PlayerPreview;
import ru.he3hauka.toolvisuals.client.config.*;
import ru.he3hauka.toolvisuals.client.gui.color.ColorEditorScreen;
import ru.he3hauka.toolvisuals.client.gui.module.VisualsButton;
import ru.he3hauka.toolvisuals.client.gui.render.UiPaint;
import ru.he3hauka.toolvisuals.client.gui.screen.VisualEditorScreen;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.CAPES;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;
import static ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign.*;

public final class CapeEditorScreen extends VisualEditorScreen {
    private static final CapeDesign CHECKER=checker();
    private static final int CELL=9,CANVAS_X=26,CANVAS_Y=148;
    private static final int[] SWATCHES={0xffb59aff,0xfff4efff,0xff303049,0xffff718e,0xff75ceff,0xff84e0b0,0xffffd477,0xff242331};
    private enum Movement { IDLE, WALK, RUN, JUMP }
    private CapeCanvas canvas;
    private CapeAnimationDraft animation;
    private final CapeTexture preview=new CapeTexture(true),checker=new CapeTexture(true),model=new CapeTexture(true);
    private final ItemStack previewWings=new ItemStack(Items.ELYTRA);
    private boolean playing=true,previewCosmetics=true,previewElytra;
    private UUID id;
    private String name,savedName,status="";
    private CapeDesign saved;
    private Face face=Face.FRONT;
    private CapeCanvas.Tool tool=CapeCanvas.Tool.BRUSH;
    private CapePresets.Kind preset=CapePresets.DEFAULT;
    private Movement movement=Movement.WALK;
    private int primary=CapePresets.PRIMARY,secondary=CapePresets.SECONDARY,radius,lastX=-1,lastY=-1;
    private boolean mirror,drawing,turning;
    private float yaw;
    private long opened=System.nanoTime();
    private VisualsButton undo,redo,save,brush;
    private EditBox nameField;
    public CapeEditorScreen(Screen parent){super(parent,text("title"));load(ToolVisualsClient.CAPES.selected());}
    public static Component text(String key,Object... args){return UiI18n.tr("toolvisuals.capes."+key,args);}
    private void load(CapeStore.Entry entry){
        status="";
        id=entry==null?null:entry.id();name=entry==null?text("default_name").getString():entry.name();
        saved=entry==null?CapePresets.create(CapePresets.DEFAULT,primary,secondary):entry.design();
        preset=CapePresets.DEFAULT;
        animation=new CapeAnimationDraft(saved);canvas=animation.canvas();savedName=name;opened=System.nanoTime();
    }
    private boolean changed(){return !name.equals(savedName)||!animation.snapshot().hash.equals(saved.hash);}
    @Override protected void init(){
        frame(820,600);
        button(20,62,90,28,text("front"),null,()->{finishStroke();face=Face.FRONT;},()->face==Face.FRONT);
        button(116,62,90,28,text("back_face"),null,()->{finishStroke();face=Face.BACK;},()->face==Face.BACK);
        button(224,62,74,28,text("color"),null,()->color(false),null);
        button(306,62,72,28,text("accent"),null,()->color(true),null);
        button(400,62,126,28,text("library"),UiPaint.Icon.STAR,()->{finishStroke();minecraft.gui.setScreen(new CapeLibraryScreen(this));},null);
        button(534,62,126,28,text("import"),null,this::chooseImport,null);
        button(668,62,132,28,text("export"),null,this::chooseExport,null);
        nameField=field(26,104,174,name,64,value->{name=value;status="";});
        save=button(224,100,254,32,text("save"),UiPaint.Icon.CHECK,this::save,null);
        button(488,100,148,32,text("new"),UiPaint.Icon.PLUS,()->guard(()->{load(null);rebuildWidgets();}),null);
        button(644,100,156,32,text("copy"),null,()->{finishStroke();id=null;status="";name=text("copy_name",name).getString();rebuildWidgets();},null);
        for(var next:CapeCanvas.Tool.values())button(224,148+next.ordinal()*34,154,28,text("tool_"+next.name().toLowerCase(Locale.ROOT)),null,()->{finishStroke();tool=next;},()->tool==next);
        brush=button(224,286,154,28,text("brush_size"),null,()->radius=(radius+1)%3,null);
        button(224,320,154,28,text("mirror"),null,()->mirror=!mirror,()->mirror);
        undo=button(224,354,73,28,text("undo"),null,()->{finishStroke();animation.undo();canvas=animation.canvas();status="";},null);
        redo=button(305,354,73,28,text("redo"),null,()->{finishStroke();animation.redo();canvas=animation.canvas();status="";},null);
        button(224,392,154,28,text("preset"),null,()->{finishStroke();minecraft.gui.setScreen(new CapePresetScreen(this,preset,primary,secondary));},null);
        for(var next:Movement.values())button(400+next.ordinal()*102,448,94,28,text("move_"+next.name().toLowerCase(Locale.ROOT)),null,()->{movement=next;opened=System.nanoTime();},()->movement==next);
        button(20,448,28,28,Component.literal("‹"),null,()->selectFrame(-1),null);
        button(54,448,28,28,Component.literal("›"),null,()->selectFrame(1),null);
        button(88,448,82,28,text("add_frame"),UiPaint.Icon.PLUS,()->{finishStroke();if(animation.duplicate()){canvas=animation.canvas();status="";}else status=text("frame_limit",MAX_FRAMES).getString();},null);
        button(176,448,82,28,text("remove_frame"),null,()->{finishStroke();animation.remove();canvas=animation.canvas();},null);
        button(264,448,114,28,text("play"),null,()->playing=!playing,()->playing);
        slider(20,490,358,text("fps"),1,MAX_FPS,1,()->animation.fps(),value->animation.fps((int)value),value->Integer.toString((int)value));
        button(400,490,194,28,text("preview_cosmetics"),null,()->previewCosmetics=!previewCosmetics,()->previewCosmetics);
        button(602,490,198,28,text("preview_elytra"),null,()->previewElytra=!previewElytra,()->previewElytra);
        button(20,536,186,28,text("enabled"),null,()->ToolVisualsClient.CONFIG.setActive(CAPES,!ToolVisualsClient.CONFIG.active(CAPES)),()->ToolVisualsClient.CONFIG.active(CAPES));
        button(224,536,154,28,text("self"),null,()->toggle(CAPE_SELF),()->ToolVisualsClient.CONFIG.flag(CAPE_SELF));
        button(400,536,194,28,text("others"),null,()->toggle(CAPE_OTHERS),()->ToolVisualsClient.CONFIG.flag(CAPE_OTHERS));
        button(602,536,198,28,text("share"),null,()->toggle(CAPE_SHARE),()->ToolVisualsClient.CONFIG.flag(CAPE_SHARE));
        button(668,568,132,26,UiI18n.tr("toolvisuals.ui.back"),UiPaint.Icon.BACK,this::onClose,null);
    }
    private void selectFrame(int delta){finishStroke();animation.select(animation.selected()+delta);canvas=animation.canvas();status="";}
    void applyPreset(CapePresets.Kind kind) {
        finishStroke();preset=kind;animation.replace(CapePresets.create(kind,primary,secondary));canvas=animation.canvas();
        status=text("preset_"+kind.name().toLowerCase(Locale.ROOT)).getString();
    }
    private void toggle(VisualSetting setting){ToolVisualsClient.CONFIG.setValue(setting,ToolVisualsClient.CONFIG.flag(setting)?0:1);}
    private void color(boolean accent){
        finishStroke();minecraft.gui.setScreen(ColorEditorScreen.single(this,text(accent?"accent":"color"),accent?secondary:primary,value->{int color=0xff000000|value;if(accent)secondary=color;else primary=color;}));
    }
    private void save(){
        finishStroke();
        try{var entry=ToolVisualsClient.CAPES.put(id,name,animation.snapshot(),true);id=entry.id();name=savedName=entry.name();saved=entry.design();nameField.setValue(name);ToolVisualsClient.CONFIG.setActive(CAPES,true);status=text("saved").getString();}
        catch(IOException error){status=text("io_error").getString();}
        catch(IllegalArgumentException error){status=text(ToolVisualsClient.CAPES.entries().size()>=CapeStore.LIMIT&&id==null?"limit":"name_error").getString();}
    }
    public void removedVariant(UUID removed){if(Objects.equals(id,removed))id=null;}
    public void choose(CapeStore.Entry entry){guard(()->{load(entry);rebuildWidgets();});}
    private void guard(Runnable action){
        finishStroke();
        if(!changed()){action.run();return;}
        minecraft.gui.setScreen(new ConfirmScreen(accepted->{minecraft.gui.setScreen(this);if(accepted)action.run();},text("discard"),text("discard_hint")));
    }
    private void chooseImport(){
        finishStroke();CapeFileDialog.choose(false,text("import").getString(),path->{if(minecraft.gui.screen()==this)importFile(path);},()->status=text("dialog_error").getString());
    }
    private void importFile(Path path){
        finishStroke();
        try{
            var image=CapePng.readImport(path);
            java.util.function.Consumer<CapeDesign> apply=design->{animation.begin();canvas.replace(design);animation.end();status=text("imported").getString();};
            if(image.requiresResize())minecraft.gui.setScreen(new CapeResizeScreen(this,image,apply));
            else apply.accept(image.fit(image.preferred));
        }
        catch(IOException|RuntimeException error){ClientErrors.report("Import cape PNG",error);status=text("png_error").getString();}
    }
    private void chooseExport(){
        finishStroke();var design=canvas.snapshot();
        CapeFileDialog.choose(true,text("export").getString(),path->{
            if(minecraft.gui.screen()!=this)return;
            Runnable write=()->{try{CapePng.write(path,design);status=text("exported").getString();}catch(IOException error){ClientErrors.report("Export cape PNG",error);status=text("io_error").getString();}};
            if(Files.exists(path))minecraft.gui.setScreen(new ConfirmScreen(accepted->{minecraft.gui.setScreen(this);if(accepted)write.run();},text("overwrite"),Component.literal(path.getFileName().toString())));else write.run();
        },()->status=text("dialog_error").getString());
    }
    @Override public void onFilesDrop(List<Path> paths){if(paths.size()==1)importFile(paths.getFirst());else status=text("png_error").getString();}
    private void finishStroke(){if(drawing){canvas.end();animation.end();drawing=false;}lastX=lastY=-1;turning=false;}
    private int pixelX(double x){return (int)Math.floor((x-px-CANVAS_X)/CELL);}
    private int pixelY(double y){return (int)Math.floor((y-py-CANVAS_Y)/CELL);}
    private boolean inside(int x,int y){return x>=0&&x<WIDTH&&y>=0&&y<HEIGHT;}
    private void draw(int x,int y){
        if(!inside(x,y)){lastX=lastY=-1;return;}
        status="";
        if(tool==CapeCanvas.Tool.PICKER){primary=canvas.pixel(face,x,y);return;}
        if(tool==CapeCanvas.Tool.FILL){canvas.fill(face,x,y,primary);return;}
        canvas.line(face,lastX<0?x:lastX,lastY<0?y:lastY,x,y,tool==CapeCanvas.Tool.ERASER?0:primary,radius,mirror);lastX=x;lastY=y;
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice){
        var point=adjusted(event);int x=pixelX(point.x()),y=pixelY(point.y());
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&inside(x,y)){setFocused(null);animation.begin();canvas.begin();drawing=true;draw(x,y);return true;}
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&point.x()>=px+400&&point.x()<px+800&&point.y()>=py+148&&point.y()<py+436){turning=true;return true;}
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&point.y()>=py+430&&point.y()<py+448&&point.x()>=px+224&&point.x()<px+384){primary=SWATCHES[Math.clamp((int)(point.x()-px-224)/20,0,7)];return true;}
        return super.mouseClicked(event,twice);
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
        if(drawing){var point=adjusted(event);draw(pixelX(point.x()),pixelY(point.y()));return true;}
        if(turning){yaw+=(float)(dx/scale);return true;}
        return super.mouseDragged(event,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){if(drawing||turning){finishStroke();return true;}return super.mouseReleased(event);}
    @Override public boolean keyPressed(KeyEvent event){
        if(!(getFocused() instanceof EditBox)&&event.hasControlDown()&&(event.key()==InputConstants.KEY_Z||event.key()==InputConstants.KEY_Y)){finishStroke();if(event.key()==InputConstants.KEY_Z)animation.undo();else animation.redo();canvas=animation.canvas();status="";return true;}
        return super.keyPressed(event);
    }
    @Override protected void paint(GuiGraphicsExtractor g,int mx,int my){
        undo.active=animation.canUndo();redo.active=animation.canRedo();save.active=!name.isBlank()&&name.codePointCount(0,name.length())<=32;
        brush.setMessage(text("brush_value",radius*2+1));
        UiPaint.rounded(g,px+20,py+140,192,304,8,palette.field);
        checker.update(CHECKER);preview.update(canvas.snapshot());
        if(checker.ready())canvasFace(g,checker.id);if(preview.ready())canvasFace(g,preview.id);
        for(int x=0;x<=WIDTH;x++)g.fill(px+CANVAS_X+x*CELL,py+CANVAS_Y,px+CANVAS_X+x*CELL+1,py+CANVAS_Y+HEIGHT*CELL,0x304e4b62);
        for(int y=0;y<=HEIGHT;y++)g.fill(px+CANVAS_X,py+CANVAS_Y+y*CELL,px+CANVAS_X+WIDTH*CELL,py+CANVAS_Y+y*CELL+1,0x304e4b62);
        UiPaint.rounded(g,px+400,py+140,400,296,8,palette.field);
        preview(g);
        label(g,text("preview_hint").getString(),410,144,380,18,.68f,palette.muted);
        for(int i=0;i<SWATCHES.length;i++)UiPaint.rounded(g,px+224+i*20,py+430,16,16,4,SWATCHES[i]);
        label(g,text("frame",animation.selected()+1,animation.size(),MAX_FRAMES).getString(),20,477,358,12,.64f,palette.muted);
        UiPaint.rounded(g,px+224,py+92,74,3,1.5f,primary);UiPaint.rounded(g,px+306,py+92,72,3,1.5f,secondary);
        String saveState=ToolVisualsClient.CAPES.status()==ru.he3hauka.toolvisuals.client.storage.JsonFile.Status.LOAD_FAILED?text("io_error").getString():id==null||changed()?text("unsaved").getString():text("saved").getString();
        String sync=text(CapeClient.shared()?"shared":CapeClient.available()?"syncing":"local").getString();
        label(g,status.isEmpty()?saveState+"  ·  "+sync:status,20,568,630,26,.72f,palette.muted);
    }
    private static CapeDesign checker(){
        int[] pixels=new int[PIXELS];for(var side:Face.values())for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++)pixels[index(side,x,y)]=(x+y)%2==0?0xff30303b:0xff3d3c48;return new CapeDesign(pixels);
    }
    private void canvasFace(GuiGraphicsExtractor g,Identifier texture){
        g.pose().pushMatrix();g.pose().translate(px+CANVAS_X,py+CANVAS_Y);g.pose().scale(CELL,CELL);
        g.blit(RenderPipelines.GUI_TEXTURED,texture,0,0,face==Face.FRONT?2:24,2,WIDTH,HEIGHT,WIDTH,HEIGHT,CapePng.ATLAS_WIDTH,CapePng.ATLAS_HEIGHT,0xffffffff);g.pose().popMatrix();
    }
    private void label(GuiGraphicsExtractor g,String text,int x,int y,int width,int height,float size,int color){UiPaint.baseline(g,UiPaint.fit(g,Component.literal(text),width,size),px+x,UiPaint.textBaseline(g,py+y,height,size),size,color,1);}
    private void preview(GuiGraphicsExtractor g){
        if(minecraft.player==null)return;
        var state=PlayerPreview.prepare(minecraft.player,false,previewCosmetics);state.bodyRot=yaw;
        state.chestEquipment=previewElytra?previewWings:ItemStack.EMPTY;
        float ticks=(float)((System.nanoTime()-opened)/50_000_000d%12000);
        boolean walking=movement==Movement.WALK||movement==Movement.RUN;
        state.ageInTicks=ticks;state.walkAnimationPos=ticks*(movement==Movement.RUN?1.1f:.65f);state.walkAnimationSpeed=walking?(movement==Movement.RUN?1:.6f):0;
        float jump=movement==Movement.JUMP?(float)Math.max(0,Math.sin(ticks*.15)):0;
        state.capeLean=walking?(movement==Movement.RUN?60:24):jump*38;state.capeLean2=walking?(float)Math.sin(ticks*.3)*5:0;state.capeFlap=(float)Math.sin(ticks*.4)*(walking?5:1)+jump*20;
        model.apply(state,playing?animation.snapshot():canvas.snapshot());
        PlayerPreview.draw(g,state,px+410,py+168,380,264,112,.9f-jump*.2f);
    }
    @Override public void removed(){finishStroke();preview.close();checker.close();model.close();super.removed();}
    @Override public void onClose(){guard(()->minecraft.gui.setScreen(parent));}
}
