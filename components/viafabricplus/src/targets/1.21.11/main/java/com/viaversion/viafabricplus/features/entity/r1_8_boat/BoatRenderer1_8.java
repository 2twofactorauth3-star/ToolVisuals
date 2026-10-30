package com.viaversion.viafabricplus.features.entity.r1_8_boat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;


public final class BoatRenderer1_8 extends AbstractBoatRenderer {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("viafabricplus", "textures/boat1_8.png");
    private final BoatModel1_8 model;

    public BoatRenderer1_8(EntityRendererProvider.Context ctx) {
        super(ctx);
        shadowRadius = 0.5F;
        model = new BoatModel1_8(ctx.bakeLayer(BoatModel1_8.MODEL_LAYER));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {
        return this.model;
    }

    @Override
    protected RenderType renderType() {
        return this.model.renderType(TEXTURE);
    }

    @Override
    public void submit(final BoatRenderState state, final PoseStack matrices, final SubmitNodeCollector orderedRenderCommandQueue, final CameraRenderState cameraRenderState) {
        matrices.pushPose();
        matrices.translate(0, 0.25, 0);
        matrices.mulPose(Axis.YP.rotationDegrees(180 - state.yRot));

        if (state.hurtTime > 0) {
            matrices.mulPose(Axis.XP.rotationDegrees(Mth.sin(state.hurtTime) * state.hurtTime * state.damageTime / 10 * state.hurtDir));
        }

        matrices.scale(-1, -1, 1);
        model.setupAnim(state);
        orderedRenderCommandQueue.submitModel(model(), state, matrices, this.renderType(), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);

        matrices.popPose();
    }

    @Override
    public net.minecraft.client.renderer.entity.state.BoatRenderState createRenderState() { return super.createRenderState(); }

}
