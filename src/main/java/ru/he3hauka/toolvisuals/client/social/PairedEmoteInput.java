package ru.he3hauka.toolvisuals.client.social;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.social.ui.PairedEmoteScreen;

public final class PairedEmoteInput {
    private PairedEmoteInput() {}

    public static void tick(Minecraft client) {
        while(ToolVisualsClient.pairedEmoteKey.consumeClick()) {
            if(client.player==null||client.level==null||!client.isWindowActive()||client.gui.screen()!=null||client.gui.overlay()!=null
                    ||!ToolVisualsClient.CONFIG.active(VisualModule.ANIMATIONS))continue;
            if(!(client.hitResult instanceof EntityHitResult hit)||!(hit.getEntity() instanceof Player target)
                    ||!PairTargets.available(client,target))continue;
            KeyMapping.releaseAll();
            client.gui.setScreen(new PairedEmoteScreen(target.getUUID()));
        }
    }
}
