package com.endplus.client.renderer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;

/**
 * Draws a glow-mask texture fullbright over the entity, with a per-frame brightness (0..1) supplied by
 * the caller so each mob can pulse in its own way.
 */
public class EmissiveFeature<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    private static final int FULL_BRIGHT = 0xF000F0;

    private final Identifier texture;
    private final BiFunction<T, Float, Float> brightness;

    public EmissiveFeature(FeatureRendererContext<T, M> context, Identifier texture, BiFunction<T, Float, Float> brightness) {
        super(context);
        this.texture = texture;
        this.brightness = brightness;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        if (entity.isInvisible()) {
            return;
        }
        float level = Math.max(0.0f, Math.min(1.0f, this.brightness.apply(entity, tickDelta)));
        int channel = (int) (level * 255.0f);
        int color = 0xFF000000 | (channel << 16) | (channel << 8) | channel;
        this.getContextModel().render(matrices, vertexConsumers.getBuffer(RenderLayer.getEyes(this.texture)),
                FULL_BRIGHT, OverlayTexture.DEFAULT_UV, color);
    }
}
