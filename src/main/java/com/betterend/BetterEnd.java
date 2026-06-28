package com.betterend;

import com.betterend.config.BetterEndConfig;
import com.betterend.registry.ModBlocks;
import com.betterend.registry.ModCreativeTabs;
import com.betterend.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterEnd implements ModInitializer {

    public static final String MOD_ID = "betterend";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static BetterEndConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = BetterEndConfig.load(FabricLoader.getInstance().getConfigDir());

        ModBlocks.initialize();
        ModItems.initialize();
        ModCreativeTabs.initialize();

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

        LOGGER.info("BetterEnd initialized");
    }
}
