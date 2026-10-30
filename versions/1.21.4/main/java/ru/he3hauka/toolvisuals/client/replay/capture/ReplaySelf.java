package ru.he3hauka.toolvisuals.client.replay.capture;

import java.util.List;
import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import ru.he3hauka.toolvisuals.client.replay.io.ReplayFrame;
import ru.he3hauka.toolvisuals.client.replay.io.ReplayFormat;

public final class ReplaySelf {
    private static final List<EquipmentSlot> SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND);
    private ItemStack[] equipment;

    public ReplayFrame capture(LocalPlayer player, long millis) {
        var bytes = Unpooled.buffer(128, ReplayFormat.MAX_PACKET_BYTES);
        var buffer = new RegistryFriendlyByteBuf(bytes, player.registryAccess());
        try {
            buffer.writeUtf(player.level().dimension().identifier().toString(), 128); buffer.writeInt(player.getId());
            buffer.writeDouble(player.getX()); buffer.writeDouble(player.getY()); buffer.writeDouble(player.getZ());
            buffer.writeFloat(player.getYRot()); buffer.writeFloat(player.getXRot()); buffer.writeFloat(player.getYHeadRot()); buffer.writeFloat(player.yBodyRot);
            buffer.writeUtf(player.getPose().name(), 32);
            buffer.writeBoolean(player.isSprinting()); buffer.writeBoolean(player.isShiftKeyDown()); buffer.writeBoolean(player.isSwimming()); buffer.writeBoolean(player.onGround());
            buffer.writeFloat(player.getHealth()); buffer.writeFloat(player.getAbsorptionAmount());
            buffer.writeInt(player.hurtTime); buffer.writeInt(player.deathTime);
            buffer.writeFloat(player.oAttackAnim);buffer.writeFloat(player.attackAnim);
            buffer.writeBoolean(player.swinging);
            if(player.swinging){buffer.writeBoolean(player.swingingArm==net.minecraft.world.InteractionHand.OFF_HAND);buffer.writeInt(player.swingTime);}
            boolean changed = equipment == null;
            if (!changed) for (int slot = 0; slot < SLOTS.size(); slot++)
                if (!ItemStack.matches(equipment[slot], player.getItemBySlot(SLOTS.get(slot)))) { changed = true; break; }
            buffer.writeBoolean(changed);
            ItemStack[] next = equipment;
            if (changed) {
                next = new ItemStack[SLOTS.size()];
                for (int slot = 0; slot < SLOTS.size(); slot++) {
                    next[slot] = player.getItemBySlot(SLOTS.get(slot)).copy();
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, next[slot]);
                }
            }
            var payload = new byte[buffer.readableBytes()]; buffer.getBytes(0, payload);
            equipment = next;
            return ReplayFrame.takeOwnership(ReplayFrame.Kind.SELF, millis, payload);
        } finally { bytes.release(); }
    }

    public static void apply(ReplayFrame frame, RegistryAccess registry, String dimension,
                             java.util.function.IntFunction<RemotePlayer> owner) {
        var bytes = Unpooled.wrappedBuffer(frame.payload.duplicate().clear());
        var buffer = new RegistryFriendlyByteBuf(bytes, registry);
        try {
            String recorded = buffer.readUtf(128); int id = buffer.readInt();
            double x = buffer.readDouble(), y = buffer.readDouble(), z = buffer.readDouble();
            float yaw = buffer.readFloat(), pitch = buffer.readFloat(), head = buffer.readFloat(), body = buffer.readFloat();
            Pose pose = Pose.valueOf(buffer.readUtf(32));
            boolean sprint = buffer.readBoolean(), crouch = buffer.readBoolean(), swim = buffer.readBoolean(), ground = buffer.readBoolean();
            float health = buffer.readFloat(), absorption = buffer.readFloat();
            int hurt = buffer.readInt(), death = buffer.readInt();float oldAttack=buffer.readFloat(),attack = buffer.readFloat();
            boolean swinging=buffer.readBoolean();
            var swingingArm=net.minecraft.world.InteractionHand.MAIN_HAND;int swingTime=0;
            if(swinging){swingingArm=buffer.readBoolean()?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND;swingTime=buffer.readInt();}
            if(swingTime<0||swingTime>1000)throw new IllegalArgumentException("Invalid recorded swing time");
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || Math.abs(x) > 30000000 || Math.abs(y) > 30000000 || Math.abs(z) > 30000000
                    || !Float.isFinite(yaw) || !Float.isFinite(pitch) || !Float.isFinite(head) || !Float.isFinite(body)
                    || !Float.isFinite(health) || !Float.isFinite(absorption) || health < 0 || health > 1000000 || absorption < 0 || absorption > 1000000
                    || hurt < 0 || hurt > 1000 || death < 0 || death > 1728000 || !Float.isFinite(attack) || attack < 0 || attack > 1
                    ||!Float.isFinite(oldAttack)||oldAttack<0||oldAttack>1)
                throw new IllegalArgumentException("Invalid recorded player pose");
            ItemStack[] equipment = null;
            if (buffer.readBoolean()) {
                equipment = new ItemStack[SLOTS.size()];
                for (int slot = 0; slot < equipment.length; slot++) equipment[slot] = ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC.decode(buffer);
            }
            if (buffer.isReadable()) throw new IllegalArgumentException("Recorded player pose has trailing bytes");
            if (!dimension.equals(recorded)) return;
            var actor = owner.apply(id);
            actor.setOldPosAndRot(); actor.setPos(x, y, z); actor.setYRot(yaw); actor.setXRot(pitch);
            actor.yHeadRot = head; actor.yBodyRot = body; actor.setPose(pose);
            actor.setSprinting(sprint); actor.setShiftKeyDown(crouch); actor.setSwimming(swim); actor.setOnGround(ground);
            actor.setHealth(health); actor.setAbsorptionAmount(absorption); actor.hurtTime = hurt; actor.deathTime = death;
            actor.oAttackAnim=oldAttack;actor.attackAnim=attack;actor.swinging=swinging;actor.swingingArm=swingingArm;actor.swingTime=swingTime;
            if (equipment != null) for (int slot = 0; slot < equipment.length; slot++) actor.setItemSlot(SLOTS.get(slot), equipment[slot]);
        } finally { bytes.release(); }
    }
}
