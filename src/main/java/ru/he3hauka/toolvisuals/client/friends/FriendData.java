package ru.he3hauka.toolvisuals.client.friends;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class FriendData {
    public static final FriendData UNKNOWN=new FriendData(false,0,0,0,List.of(),List.of());
    public final boolean healthKnown;
    public final float health,maximum,absorption;
    public final List<ItemStack> armor,inventory;
    public FriendData(boolean healthKnown,float health,float maximum,float absorption,List<ItemStack> armor,List<ItemStack> inventory){this.healthKnown=healthKnown;this.health=health;this.maximum=maximum;this.absorption=absorption;this.armor=List.copyOf(armor);this.inventory=List.copyOf(inventory);}
}
