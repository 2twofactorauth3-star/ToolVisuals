package ru.he3hauka.toolvisuals.mixin.menu;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.connection.featured.FeaturedServer;
import ru.he3hauka.toolvisuals.client.connection.featured.FeaturedServerStyle;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class FeaturedServerEntryMixin {
    @Shadow @Final private ServerData serverData;

    @Inject(method="swap", at=@At("HEAD"), cancellable=true)
    private void toolvisuals$keepFeaturedFirst(int first,int second,CallbackInfo ci) {
        if(!FeaturedServer.canSwap(first,second))ci.cancel();
    }

    @WrapOperation(method="extractContent",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"))
    private void toolvisuals$featuredTitle(GuiGraphicsExtractor graphics,Font font,String name,int x,int y,int color,Operation<Void> original){
        if(FeaturedServer.matches(serverData))graphics.text(font,FeaturedServerStyle.title(ToolVisualsClient.CONFIG.language()),x,y,color);
        else original.call(graphics,font,name,x,y,color);
    }

}
