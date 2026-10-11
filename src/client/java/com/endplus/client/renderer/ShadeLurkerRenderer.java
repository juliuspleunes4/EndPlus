package com.endplus.client.renderer;

import com.endplus.client.model.ShadeLurkerModel;
import com.endplus.entity.mob.ShadeLurkerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * In the dark the Lurker's body all but disappears, leaving only its glowing eyes; in light it is
 * fully visible. The fade eases in and out.
 */
public class ShadeLurkerRenderer extends MobEntityRenderer<ShadeLurkerEntity, ShadeLurkerModel<ShadeLurkerEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/shade_lurker.png");
    private static final Identifier GLOW_TEXTURE = Identifier.of("endplus", "textures/entity/shade_lurker_glow.png");
    private static final float BASE_SHADOW = 0.7f;
    private static final float HIDDEN_ALPHA = 0.04f;
    private static final float SCALE = 1.4f;

    public ShadeLurkerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ShadeLurkerModel<>(ctx.getPart(ShadeLurkerModel.LAYER_LOCATION)), BASE_SHADOW);
        this.addFeature(new EyesFeature(this));
    }

    @Override
    public Identifier getTexture(ShadeLurkerEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(ShadeLurkerEntity entity, MatrixStack matrices, float tickDelta) {
        matrices.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public void render(ShadeLurkerEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        float alpha = MathHelper.lerp(entity.getShadowProgress(tickDelta), 1.0f, HIDDEN_ALPHA);
        this.shadowRadius = BASE_SHADOW * alpha;
        if (alpha >= 0.999f) {
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }
        // Only the body fades; the eyes layer keeps drawing at full strength.
        RenderLayer body = RenderLayer.getEntityTranslucent(TEXTURE);
        super.render(entity, yaw, tickDelta, matrices,
                layer -> layer == body ? new AlphaVertexConsumer(vertexConsumers.getBuffer(layer), alpha) : vertexConsumers.getBuffer(layer),
                light);
    }

    @Override
    protected RenderLayer getRenderLayer(ShadeLurkerEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        if (showBody && entity.getShadowProgress(1.0f) > 0.0f) {
            return RenderLayer.getEntityTranslucent(this.getTexture(entity));
        }
        return super.getRenderLayer(entity, showBody, translucent, showOutline);
    }

    /** Fullbright eyes (and faint veins) that stay lit however dark it is. */
    private static class EyesFeature extends FeatureRenderer<ShadeLurkerEntity, ShadeLurkerModel<ShadeLurkerEntity>> {
        private static final int FULL_BRIGHT = 0xF000F0;

        EyesFeature(FeatureRendererContext<ShadeLurkerEntity, ShadeLurkerModel<ShadeLurkerEntity>> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                           ShadeLurkerEntity entity, float limbAngle, float limbDistance, float tickDelta,
                           float animationProgress, float headYaw, float headPitch) {
            if (entity.isInvisible()) {
                return;
            }
            VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEyes(GLOW_TEXTURE));
            this.getContextModel().render(matrices, consumer, FULL_BRIGHT, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
        }
    }
}
