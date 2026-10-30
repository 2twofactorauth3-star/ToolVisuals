package ru.he3hauka.toolvisuals.mixin.optimization;

import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.*;

import ru.he3hauka.toolvisuals.client.optimization.blockentity.*;

@Mixin(ChestBlockEntity.class)
public abstract class PreparedChestMixin implements PreparedChest {
    @Unique private ChestPreparation toolvisuals$preparation;
    @Override public ChestPreparation toolvisuals$preparation(){
        if(toolvisuals$preparation==null)toolvisuals$preparation=new ChestPreparation();
        return toolvisuals$preparation;
    }
}
