package ru.he3hauka.toolvisuals.client.potion.tracker;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;


public final class PotionScale {
    private PotionScale(){}
    public static boolean instant(MobEffect effect){return effect.isInstantenous();}
    public static float value(ItemStack item){return 1;}
}
