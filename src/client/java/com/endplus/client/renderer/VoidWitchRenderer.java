package com.endplus.client.renderer;

import com.endplus.client.model.VoidWitchModel;
import com.endplus.entity.minion.VoidWitchEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class VoidWitchRenderer extends MobEntityRenderer<VoidWitchEntity, VoidWitchModel<VoidWitchEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/void_witch.png");

    public VoidWitchRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new VoidWitchModel<>(ctx.getPart(VoidWitchModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public Identifier getTexture(VoidWitchEntity entity) {
        return TEXTURE;
    }
}
