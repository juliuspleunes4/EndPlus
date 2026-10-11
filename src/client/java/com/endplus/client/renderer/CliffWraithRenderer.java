package com.endplus.client.renderer;

import com.endplus.client.model.CliffWraithModel;
import com.endplus.entity.mob.CliffWraithEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CliffWraithRenderer extends MobEntityRenderer<CliffWraithEntity, CliffWraithModel<CliffWraithEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/cliff_wraith.png");

    public CliffWraithRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new CliffWraithModel<>(ctx.getPart(CliffWraithModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public Identifier getTexture(CliffWraithEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CliffWraithEntity entity, MatrixStack matrices, float tickDelta) {
        matrices.scale(1.5f, 1.5f, 1.5f);
    }
}
