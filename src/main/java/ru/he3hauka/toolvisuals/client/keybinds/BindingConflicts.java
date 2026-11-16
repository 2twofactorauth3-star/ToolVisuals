package ru.he3hauka.toolvisuals.client.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.connection.policy.FeatureKeys;
import ru.he3hauka.toolvisuals.client.localization.UiI18n;

public final class BindingConflicts {
    private BindingConflicts(){}
    public static List<String> find(InputConstants.Key key,KeyMapping edited,UUID custom){
        if(key.equals(InputConstants.UNKNOWN))return List.of();var result=new ArrayList<String>();
        for(var mapping:Minecraft.getInstance().options.keyMappings)if(FeatureKeys.allowed(ToolVisualsClient.CONFIG,mapping.getName())&&mapping!=edited&&mapping.saveString().equals(key.getName()))result.add(Component.translatable(mapping.getName()).getString());
        for(var binding:Keybinds.STORE.entries())if(binding.available(ToolVisualsClient.CONFIG)&&!binding.id().equals(custom)&&binding.key().equals(key.getName()))result.add(binding.name());return List.copyOf(result);
    }
    public static void confirm(Screen parent,InputConstants.Key key,List<String> conflicts,Runnable accepted){
        if(conflicts.isEmpty()){accepted.run();return;}
        var client=Minecraft.getInstance();String names=String.join(", ",conflicts.subList(0,Math.min(8,conflicts.size())))+(conflicts.size()>8?" +"+(conflicts.size()-8):"");
        client.gui.setScreen(new ConfirmScreen(yes->{client.gui.setScreen(parent);if(yes)accepted.run();},UiI18n.tr("toolvisuals.keybinds.conflict_title"),UiI18n.tr("toolvisuals.keybinds.conflict",key.getDisplayName(),names)));
    }
}
