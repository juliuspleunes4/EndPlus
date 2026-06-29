package com.endplus;

import com.endplus.entity.minion.*;
import com.endplus.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class EndPlusClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.VOID_IMP, PlaceholderRenderer::new);
        EntityRendererRegistry.register(ModEntities.ENDER_PHANTOM, PlaceholderRenderer::new);
        EntityRendererRegistry.register(ModEntities.ENDRITE_GOLEM, PlaceholderRenderer::new);
        EntityRendererRegistry.register(ModEntities.VOID_WITCH, PlaceholderRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHADOW_DRAKE, PlaceholderRenderer::new);
    }

    private static class PlaceholderRenderer<T extends Entity> extends EntityRenderer<T> {
        private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/entity/zombie/zombie.png");

        public PlaceholderRenderer(EntityRendererFactory.Context ctx) {
            super(ctx);
        }

        @Override
        public Identifier getTexture(T entity) {
            return TEXTURE;
        }
    }
}
