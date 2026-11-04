package ru.he3hauka.toolvisuals.client.potion.liquid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.BUFF_TRACKER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.BUFF_LIQUID;

public final class PotionLiquid {
    public interface Layers {ItemStackRenderState.LayerRenderState[] toolvisuals$potionLayers();int toolvisuals$potionLayerCount();}
    public interface Layer {void toolvisuals$drain(float fill);}
    private PotionLiquid(){}
    public static void apply(ItemStackRenderState state,ItemStack stack,ItemDisplayContext context,LivingEntity player){
        var config=ToolVisualsClient.CONFIG;
        if(!config.active(BUFF_TRACKER)||!config.flag(BUFF_LIQUID)||player==null||!player.isUsingItem()||!stack.is(Items.POTION)
                ||!ItemStack.isSameItemSameComponents(stack,player.getUseItem())||!(context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND||context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND))return;
        int duration=stack.getUseDuration(player);if(duration<=0)return;
        float partial=Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float fill=Math.clamp((player.getUseItemRemainingTicks()-partial)/duration,0,1);
        PotionGeometry.mark(state,fill);
        if(state instanceof Layers layers){var values=layers.toolvisuals$potionLayers();for(int i=0;i<Math.min(values.length,layers.toolvisuals$potionLayerCount());i++)if(values[i] instanceof Layer layer)layer.toolvisuals$drain(fill);}
    }
}
