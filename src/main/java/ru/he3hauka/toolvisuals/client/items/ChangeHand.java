package ru.he3hauka.toolvisuals.client.items;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.CHANGE_HAND;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.CHANGE_HAND_TRIGGER;

public final class ChangeHand {
    private ChangeHand() {}
    public static void key(Minecraft client) {
        if (ToolVisualsClient.CONFIG.active(CHANGE_HAND) && ToolVisualsClient.CONFIG.value(CHANGE_HAND_TRIGGER) != 1) toggle(client);
    }
    public static void attacked(Entity target) {
        if (target instanceof LivingEntity living && living.isAlive() && ToolVisualsClient.CONFIG.active(CHANGE_HAND)
                && ToolVisualsClient.CONFIG.value(CHANGE_HAND_TRIGGER) != 0) toggle(Minecraft.getInstance());
    }
    private static void toggle(Minecraft client) {
        if (client.player == null) return;
        var hand = client.options.mainHand();
        hand.set(hand.get() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
        if(ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.active())ru.he3hauka.toolvisuals.client.diagnostics.ModuleLog.action(CHANGE_HAND,"changed","hand="+hand.get());
        client.options.save(); client.options.broadcastOptions();
    }
}
