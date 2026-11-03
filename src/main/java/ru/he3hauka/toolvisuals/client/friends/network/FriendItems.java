package ru.he3hauka.toolvisuals.client.friends.network;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import ru.he3hauka.toolvisuals.network.friends.FriendWire;

public final class FriendItems {
    private FriendItems(){}
    public static List<ItemStack> decode(List<FriendWire.Item> items){return items.stream().map(FriendItems::decode).toList();}
    private static ItemStack decode(FriendWire.Item item){
        if(item.count()==0)return ItemStack.EMPTY;
        var key=Identifier.parse(item.key());boolean known=BuiltInRegistries.ITEM.containsKey(key);
        var type=known?BuiltInRegistries.ITEM.getValue(key):net.minecraft.world.item.Items.BARRIER;
        var stack=new ItemStack(type,item.count());
        if(!known)stack.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal(item.key()));
        if(item.maximum()>0){stack.set(DataComponents.MAX_DAMAGE,item.maximum());stack.setDamageValue(item.damage());}
        if(item.enchanted())stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);
        return stack;
    }
}
