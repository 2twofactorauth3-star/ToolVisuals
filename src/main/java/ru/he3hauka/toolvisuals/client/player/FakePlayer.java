package ru.he3hauka.toolvisuals.client.player;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.sound.SoundCustomizer;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.FAKE_PLAYER;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class FakePlayer {
    private static ClientLevel level;
    private static RemotePlayer entity;
    private static boolean enabled;
    private static int nextId = -1000, idleTicks;
    private FakePlayer() {}
    public static boolean owns(Entity target) { return entity != null && target == entity; }
    public static RemotePlayer current() { return entity; }
    public static void tick(Minecraft client) {
        boolean wanted = ToolVisualsClient.CONFIG.active(FAKE_PLAYER);
        if (level != client.level) { remove(); level = client.level; enabled = false; }
        if (!wanted || client.player == null) { remove(); enabled = false; return; }
        if (!enabled) spawn(client);
        enabled = true;
        if (entity != null && entity.isRemoved()) entity = null;
        if (entity != null && entity.isAlive() && ToolVisualsClient.CONFIG.flag(FAKE_PLAYER_RESTORE) && ++idleTicks > 40)
            entity.setHealth(entity.getMaxHealth());
    }
    public static void spawn(Minecraft client) {
        if (client.level == null || client.player == null || !ToolVisualsClient.CONFIG.available(FAKE_PLAYER)) return;
        remove(); level = client.level; enabled = true; idleTicks = 0;
        var source = client.player;
        PlayerSkin skin = source.getSkin(); HumanoidArm arm = source.getMainArm();
        entity = new RemotePlayer(level,new GameProfile(UUID.randomUUID(),source.getGameProfile().name())) {
            @Override public PlayerSkin getSkin() { return skin; }
            @Override public HumanoidArm getMainArm() { return arm; }
            @Override public boolean isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart part) { return source.isModelPartShown(part); }
            @Override public boolean isPushable() { return false; }
        };
        while (level.getEntity(nextId) != null) nextId--;
        entity.setId(nextId--);
        var direction = source.getLookAngle().multiply(1,0,1).normalize();
        var position = source.position().add(direction.scale(2.5));
        entity.setPos(position.x,position.y,position.z); entity.setYRot(source.getYRot() + 180); entity.setXRot(0);
        entity.yHeadRot = entity.yHeadRotO = entity.getYRot(); entity.yBodyRot = entity.yBodyRotO = entity.getYRot();
        entity.setOldPosAndRot(); entity.setNoGravity(true);
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ToolVisualsClient.CONFIG.value(FAKE_PLAYER_HEALTH));
        entity.setHealth(entity.getMaxHealth());
        if (ToolVisualsClient.CONFIG.flag(FAKE_PLAYER_EQUIPMENT))
            for (var slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND,EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})
                entity.setItemSlot(slot,source.getItemBySlot(slot).copy());
        level.addEntity(entity);
    }
    public static void remove() {
        if (entity != null) {
            if (level != null && level.getEntity(entity.getId()) == entity) level.removeEntity(entity.getId(),Entity.RemovalReason.DISCARDED);
            entity = null;
        }
    }
    public static void toggle(Minecraft client) { if (entity == null) spawn(client); else remove(); }
    public static boolean attack(Player player, Entity target) {
        if (!owns(target)) return false;
        if (!entity.isAlive() || entity.hurtTime > 5) return true;
        idleTicks = 0;
        float damage = (float)Math.max(1,player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        float remaining = entity.getHealth() - damage;
        entity.setHealth(remaining <= 0 && ToolVisualsClient.CONFIG.flag(FAKE_PLAYER_RESTORE) ? entity.getMaxHealth() : Math.max(0,remaining));
        entity.animateHurt(player.getYRot()); entity.hurtDuration = entity.hurtTime = 10;
        SoundCustomizer.hit();
        entity.setLastHurtByPlayer(player,40); player.setLastHurtMob(entity);
        return true;
    }
}
