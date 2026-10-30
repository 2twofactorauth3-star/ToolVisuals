package ru.he3hauka.toolvisuals.client.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.friends.FriendRenderState;
import ru.he3hauka.toolvisuals.client.hud.armor.ArmorHud;
import ru.he3hauka.toolvisuals.client.render.hitcolor.HitColors;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.ARMOR_COLOR;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ArmorTint {
    public interface Preview { boolean toolvisuals$armorPreview(); void toolvisuals$armorPreview(boolean value); }
    private ArmorTint() {}

    public static int color(Object state, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        int base = baseColor(state, stack);
        var config = ToolVisualsClient.CONFIG;
        return hit(state) ? HitColors.armorColor(base, config.color(HIT_TINT), config.number(HIT_STRENGTH)) : base;
    }
    public static boolean hit(Object state) {
        return (!(state instanceof Preview preview) || !preview.toolvisuals$armorPreview())
                && HitColors.armorActive(ToolVisualsClient.CONFIG, state);
    }
    private static int baseColor(Object state, ItemStack stack) {
        if (!(state instanceof AvatarRenderState avatar) || stack.isEmpty()) return 0;
        boolean preview = ((Preview)avatar).toolvisuals$armorPreview();
        if (!preview && state instanceof FriendRenderState friend && friend.toolvisuals$friendTint() != 0) return friend.toolvisuals$friendTint();
        if (stack.getMaxDamage() <= 0) return 0;
        var config = ToolVisualsClient.CONFIG;
        if (!preview && (!config.active(ARMOR_COLOR) || Minecraft.getInstance().player != null
                && avatar.id == Minecraft.getInstance().player.getId() && !config.flag(ARMOR_STATUS_SELF))) return 0;
        int index = ArmorColors.index(config, ArmorHud.remaining(stack) * 100);
        if (index < 0) return 0;
        return Math.round(config.number(ARMOR_TINT_STRENGTH) * 255) << 24 | config.color(ArmorColors.COLORS[index]);
    }
    public static boolean filled(Object state) {
        return state instanceof FriendRenderState friend&&friend.toolvisuals$friendTint()!=0&&friend.toolvisuals$friendArmorFill()
                &&(!(state instanceof Preview preview)||!preview.toolvisuals$armorPreview());
    }

}
