package com.endplus.client.renderer;

import com.endplus.client.model.VoidImpModel;
import com.endplus.entity.minion.VoidImpEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class VoidImpRenderer extends MobEntityRenderer<VoidImpEntity, VoidImpModel<VoidImpEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/void_imp.png");

    public VoidImpRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new VoidImpModel<>(ctx.getPart(VoidImpModel.LAYER_LOCATION)), 0.4f);
    }

    @Override
    public Identifier getTexture(VoidImpEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(VoidImpEntity entity, MatrixStack matrices, float tickDelta) {
        matrices.scale(0.7f, 0.7f, 0.7f);
    }
}
