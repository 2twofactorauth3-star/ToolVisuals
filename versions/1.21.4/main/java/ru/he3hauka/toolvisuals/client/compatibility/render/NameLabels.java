package ru.he3hauka.toolvisuals.client.compatibility.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;

public final class NameLabels {
    private NameLabels() {}
    public static void draw(EntityRenderState state,PoseStack pose,MultiBufferSource buffers,int light,double height,Component text){
        if(state.nameTag==null||state.nameTagAttachment==null)return;var client=Minecraft.getInstance();var attachment=state.nameTagAttachment;pose.pushPose();
        try{pose.translate(attachment.x,attachment.y+.5+height,attachment.z);pose.mulPose(client.getEntityRenderDispatcher().cameraOrientation());pose.scale(.025f,-.025f,.025f);float x=-client.font.width(text)/2f;if(!state.isDiscrete)client.font.drawInBatch(text,x,0,0xffffffff,false,pose.last().pose(),buffers,Font.DisplayMode.SEE_THROUGH,0,light);client.font.drawInBatch(text,x,0,0xffffffff,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,light);}finally{pose.popPose();}
    }
}
