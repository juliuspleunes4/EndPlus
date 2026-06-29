package com.endplus.client.renderer;

import com.endplus.client.model.EndriteGolemModel;
import com.endplus.entity.minion.EndriteGolemEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EndriteGolemRenderer extends MobEntityRenderer<EndriteGolemEntity, EndriteGolemModel<EndriteGolemEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/endrite_golem.png");

    public EndriteGolemRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new EndriteGolemModel<>(ctx.getPart(EndriteGolemModel.LAYER_LOCATION)), 1.0f);
    }

    @Override
    public Identifier getTexture(EndriteGolemEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EndriteGolemEntity entity, MatrixStack matrices, float tickDelta) {
        matrices.scale(1.5f, 1.8f, 1.5f);
    }
}
