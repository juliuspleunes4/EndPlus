package com.endplus;

import com.endplus.command.EndPlusCommand;
import com.endplus.config.EndPlusConfig;
import com.endplus.registry.ModBlocks;
import com.endplus.registry.ModCreativeTabs;
import com.endplus.registry.ModEffects;
import com.endplus.registry.ModEntities;
import com.endplus.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPlus implements ModInitializer {

    public static final String MOD_ID = "endplus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static EndPlusConfig CONFIG;
    public static final String MOD_VERSION = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");

    @Override
    public void onInitialize() {
        CONFIG = EndPlusConfig.load(FabricLoader.getInstance().getConfigDir());

        ModEffects.initialize();
        ModEntities.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        ModCreativeTabs.initialize();
        EndPlusCommand.register();

        StrippableBlockRegistry.register(ModBlocks.UMBRAL_LOG, ModBlocks.STRIPPED_UMBRAL_LOG);
        StrippableBlockRegistry.register(ModBlocks.UMBRAL_WOOD, ModBlocks.STRIPPED_UMBRAL_WOOD);

        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();
        flammable.add(ModBlocks.UMBRAL_LOG, 5, 5);
        flammable.add(ModBlocks.UMBRAL_WOOD, 5, 5);
        flammable.add(ModBlocks.STRIPPED_UMBRAL_LOG, 5, 5);
        flammable.add(ModBlocks.STRIPPED_UMBRAL_WOOD, 5, 5);
        flammable.add(ModBlocks.UMBRAL_PLANKS, 5, 20);
        flammable.add(ModBlocks.UMBRAL_SLAB, 5, 20);
        flammable.add(ModBlocks.UMBRAL_STAIRS, 5, 20);
        flammable.add(ModBlocks.UMBRAL_LEAVES, 30, 60);

        FuelRegistry fuel = FuelRegistry.INSTANCE;
        fuel.add(ModBlocks.UMBRAL_LOG.asItem(), 300);
        fuel.add(ModBlocks.UMBRAL_WOOD.asItem(), 300);
        fuel.add(ModBlocks.STRIPPED_UMBRAL_LOG.asItem(), 300);
        fuel.add(ModBlocks.STRIPPED_UMBRAL_WOOD.asItem(), 300);
        fuel.add(ModBlocks.UMBRAL_PLANKS.asItem(), 300);
        fuel.add(ModBlocks.UMBRAL_SLAB.asItem(), 150);
        fuel.add(ModBlocks.UMBRAL_STAIRS.asItem(), 300);

        LOGGER.info("End+ initialized");
    }
}
