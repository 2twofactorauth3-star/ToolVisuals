package ru.he3hauka.toolvisuals.mixin.resourcepacks;

import java.net.URL;
import java.util.UUID;

import net.minecraft.client.resources.server.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.utility.packs.PackSpoofSession;

@Mixin(DownloadedPackSource.class)
public abstract class ResourcePackSpooferMixin {
    @Shadow @Final private ServerPackManager manager;
    @Shadow private PackLoadFeedback packFeedback;
    @Unique private final PackSpoofSession toolvisuals$spoofed=new PackSpoofSession();
    @Unique private void toolvisuals$feedback(PackSpoofSession.Event event) {
        switch(event.stage()) {
            case ACCEPTED,DOWNLOADED->packFeedback.reportUpdate(event.id(),PackLoadFeedback.Update.valueOf(event.stage().name()));
            default->packFeedback.reportFinalResult(event.id(),PackLoadFeedback.FinalResult.valueOf(event.stage().name()));
        }
    }
    @Inject(method="pushPack",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$skipDownload(UUID id,URL url,String hash,CallbackInfo ci) {
        if(!ToolVisualsClient.CONFIG.active(VisualModule.RESOURCE_PACK_SPOOFER))return;
        var consent=((ServerPackManagerAccessor)manager).toolvisuals$consent();
        toolvisuals$spoofed.request(id,PackSpoofSession.Consent.valueOf(consent.name()),this::toolvisuals$feedback);ci.cancel();
    }
    @Inject(method="allowServerPacks",at=@At("TAIL"))
    private void toolvisuals$accept(CallbackInfo ci){toolvisuals$spoofed.decide(true,this::toolvisuals$feedback);}
    @Inject(method="rejectServerPacks",at=@At("TAIL"))
    private void toolvisuals$decline(CallbackInfo ci){toolvisuals$spoofed.decide(false,this::toolvisuals$feedback);}
    @Inject(method="popPack",at=@At("HEAD"))
    private void toolvisuals$pop(UUID id,CallbackInfo ci){toolvisuals$spoofed.pop(id);}
    @Inject(method={"popAll","cleanupAfterDisconnect"},at=@At("HEAD"))
    private void toolvisuals$clear(CallbackInfo ci){toolvisuals$spoofed.clear();}
}
