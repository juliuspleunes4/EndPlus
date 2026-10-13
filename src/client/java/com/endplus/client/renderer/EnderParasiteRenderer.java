package com.endplus.client.renderer;

import com.endplus.client.model.EnderParasiteModel;
import com.endplus.entity.mob.EnderParasiteEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class EnderParasiteRenderer extends MobEntityRenderer<EnderParasiteEntity, EnderParasiteModel<EnderParasiteEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/ender_parasite.png");
    private static final Identifier GLOW_TEXTURE = Identifier.of("endplus", "textures/entity/ender_parasite_glow.png");

    public EnderParasiteRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new EnderParasiteModel<>(ctx.getPart(EnderParasiteModel.LAYER_LOCATION)), 0.3f);
        // The abdomen glows like a slow heartbeat, and races while the parasite is feeding.
        this.addFeature(new EmissiveFeature<>(this, GLOW_TEXTURE, (parasite, tickDelta) -> {
            float rate = parasite.isLatched() ? 0.5f : 0.12f;
            return 0.4f + 0.6f * (0.5f + 0.5f * MathHelper.sin((parasite.age + tickDelta) * rate));
        }));
    }

    @Override
    public Identifier getTexture(EnderParasiteEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EnderParasiteEntity entity, MatrixStack matrices, float tickDelta) {
        matrices.scale(1.3f, 1.3f, 1.3f);
    }

    /** Hanging parasites cling to the ceiling, so they are drawn upside down. */
    @Override
    protected void setupTransforms(EnderParasiteEntity entity, MatrixStack matrices, float animationProgress,
                                   float bodyYaw, float tickDelta, float scale) {
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta, scale);
        if (entity.isHanging()) {
            matrices.translate(0.0f, entity.getHeight(), 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
        }
    }
}
