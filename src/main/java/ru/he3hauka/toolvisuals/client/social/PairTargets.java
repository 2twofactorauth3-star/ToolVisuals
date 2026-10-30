package ru.he3hauka.toolvisuals.client.social;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import ru.he3hauka.toolvisuals.client.player.FakePlayer;

public final class PairTargets {
    private PairTargets() {}

    public static boolean available(Minecraft client,Player target) {
        var local=client.player;
        return local!=null&&client.level!=null&&local.isAlive()&&!local.isSpectator()
                &&target!=null&&target!=local&&target.isAlive()&&!target.isSpectator()
                &&target.level()==client.level&&local.distanceToSqr(target)<=16&&!FakePlayer.owns(target);
    }
}
