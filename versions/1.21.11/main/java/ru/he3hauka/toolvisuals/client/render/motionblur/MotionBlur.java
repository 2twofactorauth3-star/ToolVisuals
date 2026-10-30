package ru.he3hauka.toolvisuals.client.render.motionblur;

import java.util.Set;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.compatibility.render.VersionCamera;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurGameAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurChainAccessor;
import ru.he3hauka.toolvisuals.mixin.motionblur.MotionBlurPassAccessor;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.MOTION_BLUR;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.MOTION_BLUR_STRENGTH;

public final class MotionBlur {
    public static final Identifier EFFECT=Identifier.parse("toolvisuals:motion_blur");
    private static final MotionHistory HISTORY=new MotionHistory();
    private static long retry;
    private MotionBlur() {}
    public static boolean active() {
        var client=Minecraft.getInstance();
        return ToolVisualsClient.CONFIG.active(MOTION_BLUR)&&ToolVisualsClient.CONFIG.value(MOTION_BLUR_STRENGTH)>0&&client.level!=null&&client.player!=null
                &&client.screen==null&&client.getOverlay()==null&&!client.getWindow().isIconified()&&!NoRender.hides(RenderOption.POST_EFFECTS);
    }
    public static void render(GameRenderer renderer) {
        if(!active()){HISTORY.clear();return;}if(System.nanoTime()<retry)return;
        var client=Minecraft.getInstance();
        try {
            var chain=client.getShaderManager().getPostChain(EFFECT,Set.of(PostChain.MAIN_TARGET_ID));if(chain==null){HISTORY.clear();return;}
            var target=client.getMainRenderTarget();var position=renderer.getMainCamera().position();
            var sample=HISTORY.sample(chain,client.level,target.width,target.height,position.x,position.y,position.z,VersionCamera.projection(),VersionCamera.view(),
                    ToolVisualsClient.CONFIG.value(MOTION_BLUR_STRENGTH),System.nanoTime());
            boolean zeroToOne=(GL.getCapabilities().OpenGL45||GL.getCapabilities().GL_ARB_clip_control)&&GL11.glGetInteger(GL45.GL_CLIP_DEPTH_MODE)==GL45.GL_ZERO_TO_ONE;
            for(var pass:((MotionBlurChainAccessor)chain).toolvisuals$motionPasses()) {
                var uniforms=((MotionBlurPassAccessor)pass).toolvisuals$motionUniforms();var buffer=uniforms.get("MotionConfig");if(buffer==null)continue;
                try(var stack=MemoryStack.stackPush()) {
                    var bytes=stack.malloc(80);sample.clipToPrevious().get(0,bytes);bytes.position(64);
                    bytes.putFloat(sample.exposureScale()).putFloat(sample.maximumPixels()).putFloat(zeroToOne?1:2).putFloat(zeroToOne?0:-1).flip();
                    if((buffer.usage()&GpuBuffer.USAGE_COPY_DST)==0){var replacement=RenderSystem.getDevice().createBuffer(()->"ToolVisuals motion blur",GpuBuffer.USAGE_UNIFORM|GpuBuffer.USAGE_COPY_DST,bytes);uniforms.put("MotionConfig",replacement);buffer.close();}
                    else RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(),bytes);
                }
            }
            chain.process(target,((MotionBlurGameAccessor)renderer).toolvisuals$motionPool());
        }catch(RuntimeException error){HISTORY.clear();retry=System.nanoTime()+5_000_000_000L;ClientErrors.report("Render motion blur",error);}
    }
}
