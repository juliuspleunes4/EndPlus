package com.endplus.client.renderer;

import com.endplus.client.model.ShadowDrakeModel;
import com.endplus.entity.minion.ShadowDrakeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class ShadowDrakeRenderer extends MobEntityRenderer<ShadowDrakeEntity, ShadowDrakeModel<ShadowDrakeEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/shadow_drake.png");

    public ShadowDrakeRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ShadowDrakeModel<>(ctx.getPart(ShadowDrakeModel.LAYER_LOCATION)), 0.8f);
    }

    @Override
    public Identifier getTexture(ShadowDrakeEntity entity) {
        return TEXTURE;
    }
}
