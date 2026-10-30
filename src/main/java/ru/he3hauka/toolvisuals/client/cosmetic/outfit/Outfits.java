package ru.he3hauka.toolvisuals.client.cosmetic.outfit;

import java.io.IOException;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

public final class Outfits {
    private Outfits() {}
    public static void wear(OutfitStore.Entry entry) throws IOException {
        if(entry.cape()==null)ToolVisualsClient.CAPES.select(null);
        else {
            var matching=ToolVisualsClient.CAPES.entries().stream().filter(cape->cape.design().equals(entry.cape())).findFirst();
            if(matching.isPresent())ToolVisualsClient.CAPES.select(matching.get().id());
            else ToolVisualsClient.CAPES.put(entry.id(),entry.name(),entry.cape(),true);
        }
        ToolVisualsClient.CONFIG.applyAppearance(entry.look(),entry.cape()!=null);
    }
    public static boolean wearing(OutfitStore.Entry entry){
        var selected=ToolVisualsClient.CAPES.selected();return entry.look().equals(ToolVisualsClient.CONFIG.accessories())
                &&java.util.Objects.equals(entry.cape(),selected==null?null:selected.design());
    }
}
