package ru.he3hauka.toolvisuals.mixin.menu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;
import ru.he3hauka.toolvisuals.client.menu.TitleMenuButtons;
import ru.he3hauka.toolvisuals.client.menu.ToolVisualsTitleMenu;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Unique private ToolVisualsTitleMenu toolvisuals$menu;
    protected TitleScreenMixin() { super(Component.empty()); }
    @Inject(method="init",at=@At("TAIL"))
    private void toolvisuals$menuControls(CallbackInfo ci) {
        toolvisuals$menu=null;
        var nativeButtons=children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
        if(ToolVisualsClient.CONFIG.customTitleMenu()) {
            clearWidgets();
            toolvisuals$menu=new ToolVisualsTitleMenu(this,nativeButtons,width,height,minecraft.getWindow().getGuiScale(),this::toolvisuals$switchMenu,this::rebuildWidgets);
            for(var button:toolvisuals$menu.widgets)addWidget(button);
        } else {
            int rowWidth=Math.min(200,width-16),half=(rowWidth-4)/2,x=(width-rowWidth)/2;
            var settings=TitleMenuButtons.find(nativeButtons,"menu.options");
            var quit=TitleMenuButtons.find(nativeButtons,"menu.quit");
            int rowY=Math.max(4,height-56);
            if(settings!=null&&quit!=null) {
                rowY=Math.min(settings.getY(),quit.getY());
                var first=TitleMenuButtons.find(nativeButtons,"menu.singleplayer");
                int firstY=first==null?height/4+48:first.getY();
                int shift=Math.max(0,rowY+44-(height-12));
                if(shift>0)for(var button:nativeButtons) {
                    if(button.getHeight()>=20&&button.getY()>=firstY&&button.getY()<=rowY&&button.getX()>=x&&button.getX()+button.getWidth()<=x+rowWidth)
                        button.setY(button.getY()-shift);
                }
                rowY-=shift;
                settings.setY(rowY+24);quit.setY(rowY+24);
            }
            addRenderableWidget(Button.builder(UiI18n.tr("toolvisuals.main.accounts"),button->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.menu.vanilla.NativeAccountsScreen(this))).bounds(x,rowY,half,20).build());
            addRenderableWidget(Button.builder(UiI18n.tr("toolvisuals.main.versions"),button->minecraft.gui.setScreen(new ru.he3hauka.toolvisuals.client.menu.vanilla.NativeVersionsScreen(this))).bounds(x+half+4,rowY,half,20).build());
            int switchWidth=Math.clamp(width/2-116,44,90);
            addRenderableWidget(Button.builder(UiI18n.tr("toolvisuals.main.switch"),button->toolvisuals$switchMenu()).bounds(8,height-32,switchWidth,20).build());
        }
    }
    @Unique private void toolvisuals$switchMenu() {
        ToolVisualsClient.CONFIG.setCustomTitleMenu(!ToolVisualsClient.CONFIG.customTitleMenu());rebuildWidgets();
    }
    @Inject(method="extractRenderState",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$customMenu(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial,CallbackInfo ci) {
        if(toolvisuals$menu==null)return;
        extractPanorama(graphics,partial);extractBlurredBackground(graphics);toolvisuals$menu.draw(graphics,mouseX,mouseY,partial);ci.cancel();
    }
    @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$customMenuClick(MouseButtonEvent event,boolean twice,CallbackInfoReturnable<Boolean> cir) {
        if(toolvisuals$menu!=null)cir.setReturnValue(super.mouseClicked(event,twice));
    }
}
