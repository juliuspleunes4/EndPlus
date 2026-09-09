package com.endplus.client.renderer;

import com.endplus.client.model.EnderPhantomModel;
import com.endplus.entity.minion.EnderPhantomEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class EnderPhantomRenderer extends MobEntityRenderer<EnderPhantomEntity, EnderPhantomModel<EnderPhantomEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/ender_phantom.png");

    public EnderPhantomRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new EnderPhantomModel<>(ctx.getPart(EnderPhantomModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public Identifier getTexture(EnderPhantomEntity entity) {
        return TEXTURE;
    }
}
