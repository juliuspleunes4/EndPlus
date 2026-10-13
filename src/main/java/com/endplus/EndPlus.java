package com.endplus;

import com.endplus.command.EndPlusCommand;
import com.endplus.config.EndPlusConfig;
import com.endplus.entity.minion.*;
import com.endplus.entity.mob.AncientEndGolemEntity;
import com.endplus.entity.mob.CliffWraithEntity;
import com.endplus.entity.mob.EnderParasiteEntity;
import com.endplus.entity.mob.PrismCrawlerEntity;
import com.endplus.entity.mob.ShadeLurkerEntity;
import com.endplus.entity.mob.VoidStalkerEntity;
import com.endplus.registry.ModBlocks;
import com.endplus.registry.ModCreativeTabs;
import com.endplus.registry.ModEffects;
import com.endplus.registry.ModEntities;
import com.endplus.registry.ModItems;
import com.endplus.registry.ModPotions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Heightmap;
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
        ModPotions.initialize();
        ModCreativeTabs.initialize();
        EndPlusCommand.register();

        FabricDefaultAttributeRegistry.register(ModEntities.VOID_IMP, VoidImpEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.ENDER_PHANTOM, EnderPhantomEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.ENDRITE_GOLEM, EndriteGolemEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.VOID_WITCH, VoidWitchEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.SHADOW_DRAKE, ShadowDrakeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.VOID_STALKER, VoidStalkerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.ENDER_PARASITE, EnderParasiteEntity.createAttributes());
        SpawnRestriction.register(ModEntities.ENDER_PARASITE, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
        FabricDefaultAttributeRegistry.register(ModEntities.ANCIENT_END_GOLEM, AncientEndGolemEntity.createAttributes());
        SpawnRestriction.register(ModEntities.ANCIENT_END_GOLEM, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnIgnoreLightLevel);
        FabricDefaultAttributeRegistry.register(ModEntities.CLIFF_WRAITH, CliffWraithEntity.createAttributes());
        SpawnRestriction.register(ModEntities.CLIFF_WRAITH, SpawnLocationTypes.UNRESTRICTED,
                Heightmap.Type.MOTION_BLOCKING, HostileEntity::canSpawnIgnoreLightLevel);
        FabricDefaultAttributeRegistry.register(ModEntities.SHADE_LURKER, ShadeLurkerEntity.createAttributes());
        SpawnRestriction.register(ModEntities.SHADE_LURKER, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnIgnoreLightLevel);
        FabricDefaultAttributeRegistry.register(ModEntities.PRISM_CRAWLER, PrismCrawlerEntity.createAttributes());
        SpawnRestriction.register(ModEntities.PRISM_CRAWLER, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnIgnoreLightLevel);
        SpawnRestriction.register(ModEntities.VOID_STALKER, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnIgnoreLightLevel);

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
