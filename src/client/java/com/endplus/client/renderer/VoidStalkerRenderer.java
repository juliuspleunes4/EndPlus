package com.endplus.client.renderer;

import com.endplus.client.model.VoidStalkerModel;
import com.endplus.entity.mob.VoidStalkerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class VoidStalkerRenderer extends MobEntityRenderer<VoidStalkerEntity, VoidStalkerModel<VoidStalkerEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/void_stalker.png");
    private static final double REVEAL_RANGE_SQ = VoidStalkerEntity.REVEAL_RANGE * VoidStalkerEntity.REVEAL_RANGE;

    public VoidStalkerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new VoidStalkerModel<>(ctx.getPart(VoidStalkerModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public Identifier getTexture(VoidStalkerEntity entity) {
        return TEXTURE;
    }

    /** A camouflaged Stalker is not drawn at all unless the local camera is within reveal range. */
    @Override
    public void render(VoidStalkerEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        if (entity.isCamouflaged()) {
            Entity viewer = MinecraftClient.getInstance().getCameraEntity();
            if (viewer == null || viewer.squaredDistanceTo(entity) > REVEAL_RANGE_SQ) {
                return;
            }
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
