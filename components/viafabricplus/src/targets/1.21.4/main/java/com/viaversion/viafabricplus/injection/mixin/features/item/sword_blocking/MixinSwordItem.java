package com.viaversion.viafabricplus.injection.mixin.features.item.sword_blocking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.raphimc.vialegacy.api.LegacyProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class MixinSwordItem extends Item {

    public MixinSwordItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (ProtocolTranslator.getTargetVersion().betweenInclusive(LegacyProtocolVersion.b1_8tob1_8_1, ProtocolVersion.v1_8)) {
            user.setCurrentHand(hand);
            return ActionResult.SUCCESS;
        } else {
            return super.use(world, user, hand);
        }
    }

}
