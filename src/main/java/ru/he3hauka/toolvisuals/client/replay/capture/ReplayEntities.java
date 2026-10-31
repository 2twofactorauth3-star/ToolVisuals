package ru.he3hauka.toolvisuals.client.replay.capture;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import ru.he3hauka.toolvisuals.mixin.replay.ReplayPlayerInfoAccessor;

public final class ReplayEntities {
    private ReplayEntities() {}

    public static void players(ClientPacketListener listener, Consumer<Packet<?>> output) {
        var actions = EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class);
        var entries = new ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>();
        if (listener.getOnlinePlayers().size() > 8192) throw new IllegalStateException("Replay snapshot exceeds the player budget");
        var listed = listener.getListedOnlinePlayers();
        for (var info : listener.getOnlinePlayers()) {
            var profile = info.getProfile();
            entries.add(new ClientboundPlayerInfoUpdatePacket.Entry(profile.id(), profile, listed.contains(info), info.getLatency(),
                    info.getGameMode(), info.getTabListDisplayName(), info.showHat(), info.getTabListOrder(),
                    info.getChatSession() == null ? null : info.getChatSession().asData()));
            if (entries.size() == 64) {
                playerPacket(actions, entries, output);
                entries.clear();
            }
        }
        if (!entries.isEmpty()) playerPacket(actions, entries, output);
    }

    private static void playerPacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions,
                                     List<ClientboundPlayerInfoUpdatePacket.Entry> entries, Consumer<Packet<?>> output) {
        var packet = new ClientboundPlayerInfoUpdatePacket(actions, List.of());
        ((ReplayPlayerInfoAccessor) packet).toolvisuals$replayEntries(List.copyOf(entries));
        output.accept(packet);
    }

    public static void capture(ClientLevel level, Entity localPlayer, Consumer<Packet<?>> output) {
        var entities = new ArrayList<Entity>();
        for (var entity : level.entitiesForRendering()) {
            if (entity == localPlayer || entity.isRemoved()) continue;
            if (entities.size() == 8191) throw new IllegalStateException("Replay snapshot exceeds the entity budget");
            entities.add(entity);
            int data = entity instanceof FallingBlockEntity falling ? Block.getId(falling.getBlockState())
                    : entity instanceof HangingEntity hanging ? hanging.getDirection().get3DDataValue()
                    : entity instanceof Projectile projectile && projectile.getOwner() != null ? projectile.getOwner().getId() : 0;
            output.accept(new ClientboundAddEntityPacket(entity.getId(), entity.getUUID(), entity.getX(), entity.getY(), entity.getZ(),
                    entity.getXRot(), entity.getYRot(), entity.getType(), data, entity.getDeltaMovement(), entity.getYHeadRot()));
            var metadata = entity.getEntityData().getNonDefaultValues();
            if (metadata != null && !metadata.isEmpty()) output.accept(new ClientboundSetEntityDataPacket(entity.getId(), metadata));
            if (entity instanceof LivingEntity living) {
                var equipment = new ArrayList<Pair<EquipmentSlot, ItemStack>>();
                for (var slot : EquipmentSlot.values()) {
                    var item = living.getItemBySlot(slot);
                    if (!item.isEmpty()) equipment.add(Pair.of(slot, item.copy()));
                }
                if (!equipment.isEmpty()) output.accept(new ClientboundSetEquipmentPacket(entity.getId(), equipment));
                var attributes = living.getAttributes().getSyncableAttributes();
                if (!attributes.isEmpty()) output.accept(new ClientboundUpdateAttributesPacket(entity.getId(), attributes));
                for (var effect : living.getActiveEffects()) output.accept(new ClientboundUpdateMobEffectPacket(entity.getId(), effect, false));
            }
        }
        for (var entity : entities) {
            if (!entity.getPassengers().isEmpty()) output.accept(new ClientboundSetPassengersPacket(entity));
            if (entity instanceof net.minecraft.world.entity.Mob mob && mob.getLeashHolder() != null)
                output.accept(new ClientboundSetEntityLinkPacket(mob, mob.getLeashHolder()));
        }
    }
}
