package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

import ru.he3hauka.toolvisuals.cosmetic.accessory.AccessoryLook;

public final class AccessoryLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
    public AccessoryLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent){super(parent);}
    @Override public void render(PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light,AvatarRenderState state,float yaw,float pitch){
        var collector=new ru.he3hauka.toolvisuals.client.compatibility.render.GeometryCollector(buffers);
        var frame=((AccessoryVisualState)state).toolvisuals$accessories();
        if(frame==null||state.isInvisible||state.isSpectator)return;
        if(frame.backVisible&&frame.look.back()!=AccessoryLook.Back.NONE){
            var model=AccessoryModels.wing(frame.look.back(),frame.far);
            for(int side=-1;side<=1;side+=2){
                pose.pushPose();
                try{
                    getParentModel().body.translateAndRotate(pose);pose.translate(side*.10,.15+frame.wingLift,.21);
                    pose.rotate(Axis.XP,frame.wingPitch);pose.rotate(Axis.YP,side*frame.wingHinge+frame.wingBank);pose.rotate(Axis.ZP,side*frame.wingRoll+frame.wingBankRoll);pose.scale(side*frame.look.backScale(),frame.look.backScale(),frame.look.backScale());
                    submit(pose,collector,model.base,frame,false);
                    pose.translate(model.pivotX,model.pivotY,model.pivotZ);
                    if(frame.look.back()==AccessoryLook.Back.ORBITAL)pose.rotate(Axis.ZP,side*frame.backDetailRotation);
                    else {pose.rotate(Axis.YP,frame.wingFlex);pose.rotate(Axis.ZP,frame.wingFlex*.12f);}
                    pose.translate(-model.pivotX,-model.pivotY,-model.pivotZ);
                    submit(pose,collector,model.detail,frame,false);
                }finally{pose.popPose();}
            }
        }
        if(frame.look.head()!=AccessoryLook.Head.NONE){
            pose.pushPose();
            try{
                getParentModel().head.translateAndRotate(pose);if(!state.headEquipment.isEmpty())pose.translate(0,-.065,0);float size=frame.look.headScale();pose.translate(0,-.5+frame.headLift,0);pose.rotate(Axis.XP,frame.headPitch);pose.rotate(Axis.YP,frame.headYaw);pose.rotate(Axis.ZP,frame.headRoll);pose.scale(size,size,size);pose.translate(0,.5,0);
                if(frame.look.head()==AccessoryLook.Head.HALO&&frame.look.animated())pose.rotate(Axis.YP,frame.headRotation);
                var model=AccessoryModels.headpiece(frame.look.head(),frame.far);
                submit(pose,collector,model.base,frame,true);
                if(frame.look.head()==AccessoryLook.Head.HALO||frame.look.head()==AccessoryLook.Head.ASTROLABE){
                    pose.translate(model.pivotX,model.pivotY,model.pivotZ);pose.rotate(Axis.YP,frame.headDetailRotation);pose.translate(-model.pivotX,-model.pivotY,-model.pivotZ);
                }
                submit(pose,collector,model.detail,frame,true);
            }finally{pose.popPose();}
        }
    }
    private static void submit(PoseStack pose,SubmitNodeCollector collector,AccessoryMesh mesh,AccessoryFrame frame,boolean head){
        if(mesh.size==0)return;
        var colors=frame.paints(head);
        collector.submitCustomGeometry(pose,AccessoryPipeline.TYPE,(matrix,vertices)->mesh.emitColored((x,y,z,color)->vertices.addVertex(matrix,x,y,z).setColor(color),colors));
    }
}
