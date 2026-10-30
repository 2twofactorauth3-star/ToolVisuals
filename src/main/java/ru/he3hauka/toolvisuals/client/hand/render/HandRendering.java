package ru.he3hauka.toolvisuals.client.hand.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.hand.HandProfiles;
import ru.he3hauka.toolvisuals.client.hand.HandSide;
import ru.he3hauka.toolvisuals.client.hand.editor.HandProjection;
import ru.he3hauka.toolvisuals.client.hand.ui.HandEditorScreen;
import static ru.he3hauka.toolvisuals.client.module.VisualModule.CUSTOM_HAND;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.HAND_CHAT_EDITOR;

public final class HandRendering {
    public static final HandProfiles PROFILES=new HandProfiles();
    public static final AABB RIGHT_ARM_BOUNDS=new AABB(-.58,-.04,-.141,-.22,.78,.141);
    public static final AABB LEFT_ARM_BOUNDS=new AABB(.22,-.04,-.141,.58,.78,.141);
    public static final AABB MAP_BOUNDS=new AABB(-.211,-.211,-.001,.211,.211,.001);
    private static final EnumMap<HandSide,HandProjection.Frame> PENDING=new EnumMap<>(HandSide.class);
    private static Map<HandSide,HandProjection.Frame> visible=Map.of();
    private static long captured;
    private static Scope current;
    public static final class Scope {
        private final HandSide side;
        private final Matrix4f basis;
        private final Scope previous;
        private Scope(HandSide side,PoseStack pose){this.side=side;basis=new Matrix4f(pose.last().pose());previous=current;}
    }
    private HandRendering() {}

    public static boolean editable() {
        var client=Minecraft.getInstance();var screen=client.gui.screen();
        return ToolVisualsClient.CONFIG.active(CUSTOM_HAND)&&client.player!=null&&client.level!=null&&client.player.isAlive()
                &&!client.player.isSpectator()&&client.options.getCameraType().isFirstPerson()&&!client.gui.hud.isHidden()
                &&(screen instanceof HandEditorScreen||screen instanceof ChatScreen&&ToolVisualsClient.CONFIG.flag(HAND_CHAT_EDITOR));
    }

    public static void beginFrame(){PENDING.clear();if(!editable())clear();}
    public static void publish(){visible=Map.copyOf(PENDING);captured=System.nanoTime();}
    public static Map<HandSide,HandProjection.Frame> bounds(){return editable()&&System.nanoTime()-captured<500_000_000?visible:Map.of();}
    public static void clear(){visible=Map.of();captured=0;current=null;}
    public static Scope begin(HandSide side,PoseStack pose){if(!editable())return null;var scope=new Scope(side,pose);current=scope;return scope;}
    public static void end(Scope scope){if(scope!=null)current=scope.previous;}

    public static void item(PoseStack pose,ItemStackRenderState item) {
        if(current==null)return;
        capture(pose,item.getModelBoundingBox());
    }
    public static AABB armBounds(net.minecraft.world.entity.HumanoidArm arm){return arm==net.minecraft.world.entity.HumanoidArm.RIGHT?RIGHT_ARM_BOUNDS:LEFT_ARM_BOUNDS;}
    public static void arm(PoseStack pose,net.minecraft.world.entity.HumanoidArm arm){if(current!=null&&current.side.arm==arm)capture(pose,armBounds(arm));}
    public static void map(PoseStack pose){if(current!=null)capture(pose,MAP_BOUNDS);}
    private static void capture(PoseStack pose,AABB box) {
        var client=Minecraft.getInstance();var camera=client.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        var screen=client.gui.screen();if(screen==null)return;
        var frame=HandProjection.project(box,pose.last().pose(),current.basis,RenderSystem.getModelViewStack(),camera.hudFov,screen.width,screen.height);
        if(frame!=null)PENDING.put(current.side,frame);
    }
}
