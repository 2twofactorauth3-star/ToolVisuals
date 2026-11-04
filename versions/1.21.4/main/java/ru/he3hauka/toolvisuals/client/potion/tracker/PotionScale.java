package ru.he3hauka.toolvisuals.client.potion.tracker;

public final class PotionScale {
    private PotionScale(){}
    public static boolean instant(net.minecraft.world.effect.MobEffect effect){return effect.isInstantenous();}
    public static float value(net.minecraft.world.item.ItemStack item){return 1;}
}
