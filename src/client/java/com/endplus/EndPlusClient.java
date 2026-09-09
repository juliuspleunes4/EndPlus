package com.endplus;

import com.endplus.client.model.*;
import com.endplus.client.renderer.*;
import com.endplus.registry.ModBlocks;
import com.endplus.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class EndPlusClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CRYSTAL_SPIRE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UMBRAL_LEAVES, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CRYSTAL_GLASS, RenderLayer.getTranslucent());

        EntityModelLayerRegistry.registerModelLayer(VoidImpModel.LAYER_LOCATION, VoidImpModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(EnderPhantomModel.LAYER_LOCATION, EnderPhantomModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(EndriteGolemModel.LAYER_LOCATION, EndriteGolemModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(VoidWitchModel.LAYER_LOCATION, VoidWitchModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ShadowDrakeModel.LAYER_LOCATION, ShadowDrakeModel::getTexturedModelData);

        EntityRendererRegistry.register(ModEntities.VOID_BEAM, FlyingItemEntityRenderer::new);

        EntityRendererRegistry.register(ModEntities.VOID_IMP, VoidImpRenderer::new);
        EntityRendererRegistry.register(ModEntities.ENDER_PHANTOM, EnderPhantomRenderer::new);
        EntityRendererRegistry.register(ModEntities.ENDRITE_GOLEM, EndriteGolemRenderer::new);
        EntityRendererRegistry.register(ModEntities.VOID_WITCH, VoidWitchRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHADOW_DRAKE, ShadowDrakeRenderer::new);
    }
}
