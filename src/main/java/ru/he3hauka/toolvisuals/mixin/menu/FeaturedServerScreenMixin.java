package ru.he3hauka.toolvisuals.mixin.menu;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.connection.featured.FeaturedServer;
import ru.he3hauka.toolvisuals.client.connection.featured.FeaturedServerBanner;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

@Mixin(JoinMultiplayerScreen.class)
public abstract class FeaturedServerScreenMixin extends Screen {
    @Unique private final FeaturedServerBanner toolvisuals$banner=new FeaturedServerBanner();
    @Shadow protected ServerSelectionList serverSelectionList;
    @Shadow private Button editButton;
    @Shadow private Button deleteButton;
    protected FeaturedServerScreenMixin(){super(Component.empty());}

    @Inject(method="repositionElements",at=@At("TAIL"))
    private void toolvisuals$bannerSpace(CallbackInfo ci){toolvisuals$banner.layout(serverSelectionList);}

    @Inject(method="init",at=@At("TAIL"))
    private void toolvisuals$addBanner(CallbackInfo ci){
        addRenderableOnly((graphics,mouseX,mouseY,partial)->toolvisuals$banner.draw(graphics,font,serverSelectionList,ToolVisualsClient.CONFIG.language()));
    }
    @Inject(method = "onSelectedChange", at = @At("TAIL"))
    private void toolvisuals$lockFeatured(CallbackInfo ci) {
        if (serverSelectionList.getSelected() instanceof ServerSelectionList.OnlineServerEntry entry
                && FeaturedServer.matches(entry.getServerData())) {
            editButton.active = false; deleteButton.active = false;
        }
    }
}
