package com.endplus.client.renderer;

import com.endplus.entity.mob.PrismCrawlerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.util.Identifier;

/** Reuses the vanilla spider model with a crystal skin. */
public class PrismCrawlerRenderer extends MobEntityRenderer<PrismCrawlerEntity, SpiderEntityModel<PrismCrawlerEntity>> {

    private static final Identifier TEXTURE = Identifier.of("endplus", "textures/entity/prism_crawler.png");

    public PrismCrawlerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new SpiderEntityModel<>(ctx.getPart(EntityModelLayers.SPIDER)), 0.8f);
    }

    @Override
    public Identifier getTexture(PrismCrawlerEntity entity) {
        return TEXTURE;
    }
}
