package com.endplus.client.renderer;

import com.endplus.client.model.VoidStalkerModel;
import com.endplus.entity.mob.VoidStalkerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Draws the purple fissures and eyes fullbright with a slow, soft pulse. Stays lit while the
 * Stalker is camouflaged (a little dimmer, so the faint silhouette gives itself away to a careful eye).
 */
public class VoidStalkerGlowFeature extends FeatureRenderer<VoidStalkerEntity, VoidStalkerModel<VoidStalkerEntity>> {

    private static final Identifier GLOW_TEXTURE = Identifier.of("endplus", "textures/entity/void_stalker_glow.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    public VoidStalkerGlowFeature(FeatureRendererContext<VoidStalkerEntity, VoidStalkerModel<VoidStalkerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       VoidStalkerEntity entity, float limbAngle, float limbDistance, float tickDelta,
                       float animationProgress, float headYaw, float headPitch) {
        if (entity.isInvisible()) {
            return;
        }
        float pulse = 0.5f + 0.5f * MathHelper.sin((entity.age + tickDelta) * 0.1f);
        float brightness = 0.35f + 0.65f * pulse;
        float cloak = entity.getCamouflageProgress(tickDelta) * (1.0f - VoidStalkerRenderer.nearness(entity));
        brightness *= 1.0f - 0.35f * cloak;

        int channel = (int) (brightness * 255.0f);
        int color = 0xFF000000 | (channel << 16) | (channel << 8) | channel;
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEyes(GLOW_TEXTURE));
        this.getContextModel().render(matrices, consumer, FULL_BRIGHT, OverlayTexture.DEFAULT_UV, color);
    }
}
