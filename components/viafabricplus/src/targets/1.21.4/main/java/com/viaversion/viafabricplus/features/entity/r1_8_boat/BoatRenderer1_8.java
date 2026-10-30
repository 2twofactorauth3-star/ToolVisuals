package com.viaversion.viafabricplus.features.entity.r1_8_boat;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.AbstractBoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BoatEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;


public final class BoatRenderer1_8 extends AbstractBoatEntityRenderer {

    private static final Identifier TEXTURE = Identifier.of("viafabricplus", "textures/boat1_8.png");
    private final BoatModel1_8 model;

    public BoatRenderer1_8(EntityRendererFactory.Context ctx) {
        super(ctx);
        shadowRadius = 0.5F;
        model = new BoatModel1_8(ctx.getPart(BoatModel1_8.MODEL_LAYER));
    }

    @Override
    protected EntityModel<BoatEntityRenderState> getModel() {
        return this.model;
    }

    @Override
    protected RenderLayer getRenderLayer() {
        return this.model.getLayer(TEXTURE);
    }

    @Override
    public void render(BoatEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0, 0.25, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - state.yaw));

        if (state.damageWobbleTicks > 0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(state.damageWobbleTicks) * state.damageWobbleTicks * state.damageWobbleStrength / 10 * state.damageWobbleSide));
        }

        matrices.scale(-1, -1, 1);
        model.setAngles(state);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(model.getLayer(TEXTURE));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    @Override
    public net.minecraft.client.render.entity.state.BoatEntityRenderState createRenderState() { return super.createRenderState(); }

}
