package ru.he3hauka.toolvisuals.mixin.optimization;

import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_HIDDEN_FACES;

@Mixin(value=AbstractBlockRenderContext.class,remap=false)
public abstract class HiddenFacesMixin {
    @Shadow protected BlockAndTintGetter level;
    @Shadow protected BlockState state;
    @Shadow protected BlockPos pos;
    @Shadow private boolean enableCulling;
    @Inject(method="isFaceCulled",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$solidNeighbor(Direction face,CallbackInfoReturnable<Boolean> callback){if(!enableCulling||face==null||!Optimizations.enabled(OPT_HIDDEN_FACES)||!state.canOcclude()||!state.isSolidRender(level,pos))return;var neighbor=pos.relative(face);var adjacent=level.getBlockState(neighbor);if(adjacent.canOcclude()&&adjacent.isSolidRender(level,neighbor)){Diagnostics.count(Diagnostics.Counter.QUADS_SKIPPED,1);callback.setReturnValue(true);}}
}
