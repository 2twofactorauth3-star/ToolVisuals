package ru.he3hauka.toolvisuals.mixin.render;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {
    @Accessor("state") RenderSetup toolvisuals$state();
    @Invoker("create") static RenderType toolvisuals$create(String name, RenderSetup setup) { throw new AssertionError(); }
}
