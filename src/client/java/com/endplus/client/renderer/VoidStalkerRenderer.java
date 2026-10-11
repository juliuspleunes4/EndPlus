package com.endplus.client.renderer;

import com.endplus.client.model.VoidStalkerModel;
import com.endplus.entity.mob.VoidStalkerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * While camouflaged the Stalker is drawn as a faint, nearly transparent silhouette, so the world
 * behind it stays visible but a careful eye can pick it out. Within {@link VoidStalkerEntity#REVEAL_RANGE} blocks of the camera it fades back to solid.
 */
public class VoidStalkerRenderer extends MobEntityRenderer<VoidStalkerEntity, VoidStalkerModel<VoidStalkerEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/void_stalker.png");
    private static final float BASE_SHADOW = 0.4f;
    private static final float FAINT_ALPHA = 0.12f;
    /** Fully solid inside this distance, fading to the faint silhouette at REVEAL_RANGE. */
    private static final double SOLID_RANGE = VoidStalkerEntity.REVEAL_RANGE - 3.0;

    public VoidStalkerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new VoidStalkerModel<>(ctx.getPart(VoidStalkerModel.LAYER_LOCATION)), BASE_SHADOW);
        this.addFeature(new VoidStalkerGlowFeature(this));
    }

    @Override
    public Identifier getTexture(VoidStalkerEntity entity) {
        return TEXTURE;
    }

    /** 0 = fully faint .. 1 = fully solid, from the camera distance. */
    static float nearness(VoidStalkerEntity entity) {
        Entity viewer = MinecraftClient.getInstance().getCameraEntity();
        if (viewer == null) {
            return 0.0f;
        }
        double dist = viewer.distanceTo(entity);
        return (float) MathHelper.clamp((VoidStalkerEntity.REVEAL_RANGE - dist) / (VoidStalkerEntity.REVEAL_RANGE - SOLID_RANGE), 0.0, 1.0);
    }

    private static float alpha(VoidStalkerEntity entity, float tickDelta) {
        float cloak = entity.getCamouflageProgress(tickDelta);
        if (cloak <= 0.0f) {
            return 1.0f;
        }
        float shimmer = FAINT_ALPHA + 0.05f * MathHelper.sin((entity.age + tickDelta) * 0.15f);
        float target = MathHelper.lerp(nearness(entity), shimmer, 1.0f);
        return MathHelper.lerp(cloak, 1.0f, target);
    }

    @Override
    public void render(VoidStalkerEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        float alpha = alpha(entity, tickDelta);
        this.shadowRadius = BASE_SHADOW * alpha;
        if (alpha >= 0.999f) {
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }
        // Only the body fades; feature layers (the glow) keep drawing at full strength.
        RenderLayer body = RenderLayer.getEntityTranslucent(TEXTURE);
        super.render(entity, yaw, tickDelta, matrices,
                layer -> layer == body ? new AlphaConsumer(vertexConsumers.getBuffer(layer), alpha) : vertexConsumers.getBuffer(layer),
                light);
    }

    @Override
    protected RenderLayer getRenderLayer(VoidStalkerEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        if (showBody && entity.getCamouflageProgress(1.0f) > 0.0f) {
            return RenderLayer.getEntityTranslucent(this.getTexture(entity));
        }
        return super.getRenderLayer(entity, showBody, translucent, showOutline);
    }

    /** Forwards everything to the real consumer but scales the alpha of each vertex colour. */
    private static final class AlphaConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alpha;

        AlphaConsumer(VertexConsumer delegate, float alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            this.delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.delegate.color(red, green, blue, (int) (alpha * this.alpha));
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            this.delegate.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            this.delegate.overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            this.delegate.light(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.delegate.normal(x, y, z);
            return this;
        }
    }
}
