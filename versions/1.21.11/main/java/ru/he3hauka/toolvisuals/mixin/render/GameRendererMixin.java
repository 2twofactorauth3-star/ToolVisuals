package ru.he3hauka.toolvisuals.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.he3hauka.toolvisuals.client.compatibility.render.VersionCamera;
import ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor;
import ru.he3hauka.toolvisuals.client.optimization.frame.FrameWork;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import static ru.he3hauka.toolvisuals.client.render.filter.RenderOption.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method="render",at=@At("HEAD")) private void toolvisuals$begin(CallbackInfo callback){FrameWork.begin();FrameMonitor.begin();}
    @Inject(method="render",at=@At("RETURN")) private void toolvisuals$end(CallbackInfo callback){FrameMonitor.end();}
    @ModifyExpressionValue(method="render",at=@At(value="FIELD",target="Lnet/minecraft/client/renderer/GameRenderer;postEffectId:Lnet/minecraft/resources/Identifier;"))
    private Identifier toolvisuals$postEffects(Identifier original){return NoRender.hides(POST_EFFECTS)?null:original;}
    @WrapOperation(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"))
    private void toolvisuals$camera(LevelRenderer renderer,GraphicsResourceAllocator allocator,DeltaTracker delta,boolean outline,Camera camera,
            Matrix4f view,Matrix4f projection,Matrix4f culling,GpuBufferSlice fog,Vector4f color,boolean sky,Operation<Void> original) {
        VersionCamera.capture(view,projection);original.call(renderer,allocator,delta,outline,camera,view,projection,culling,fog,color,sky);
    }
    @Inject(method="bobHurt",at=@At("HEAD"),cancellable=true) private void toolvisuals$hurt(CallbackInfo callback){if(NoRender.hides(HURT_CAMERA))callback.cancel();}
    @Inject(method="bobView",at=@At("HEAD"),cancellable=true) private void toolvisuals$bob(CallbackInfo callback){if(NoRender.hides(VIEW_BOBBING))callback.cancel();}
    @Inject(method="renderItemInHand",at=@At("HEAD"),cancellable=true) private void toolvisuals$hands(CallbackInfo callback){if(NoRender.hides(FIRST_PERSON_HANDS))callback.cancel();}
}
