package ru.he3hauka.toolvisuals.client.potion.tracker;

public final class PotionScale {
    private PotionScale(){}
    public static boolean instant(net.minecraft.world.effect.MobEffect effect){return effect.isInstantaneous();}
    public static float value(net.minecraft.world.item.ItemStack item){float scale=item.getOrDefault(net.minecraft.core.component.DataComponents.POTION_DURATION_SCALE,1f);return Float.isFinite(scale)&&scale>0?scale:1;}
}
