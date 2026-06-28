package com.endplus.registry;

import com.endplus.EndPlus;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item CRACKED_END_STONE = registerBlockItem("cracked_end_stone", ModBlocks.CRACKED_END_STONE);
    public static final Item VOID_TOUCHED_PURPUR = registerBlockItem("void_touched_purpur", ModBlocks.VOID_TOUCHED_PURPUR);
    public static final Item UMBRAL_LOG = registerBlockItem("umbral_log", ModBlocks.UMBRAL_LOG);
    public static final Item UMBRAL_WOOD = registerBlockItem("umbral_wood", ModBlocks.UMBRAL_WOOD);
    public static final Item STRIPPED_UMBRAL_LOG = registerBlockItem("stripped_umbral_log", ModBlocks.STRIPPED_UMBRAL_LOG);
    public static final Item STRIPPED_UMBRAL_WOOD = registerBlockItem("stripped_umbral_wood", ModBlocks.STRIPPED_UMBRAL_WOOD);
    public static final Item UMBRAL_PLANKS = registerBlockItem("umbral_planks", ModBlocks.UMBRAL_PLANKS);
    public static final Item UMBRAL_SLAB = registerBlockItem("umbral_slab", ModBlocks.UMBRAL_SLAB);
    public static final Item UMBRAL_STAIRS = registerBlockItem("umbral_stairs", ModBlocks.UMBRAL_STAIRS);
    public static final Item UMBRAL_LEAVES = registerBlockItem("umbral_leaves", ModBlocks.UMBRAL_LEAVES);
    public static final Item VOIDMOSS = registerBlockItem("voidmoss", ModBlocks.VOIDMOSS);
    public static final Item CRYSTAL_SPIRE = registerBlockItem("crystal_spire", ModBlocks.CRYSTAL_SPIRE);
    public static final Item ENDRITE_ORE = registerBlockItem("endrite_ore", ModBlocks.ENDRITE_ORE);
    public static final Item VOID_ORE = registerBlockItem("void_ore", ModBlocks.VOID_ORE);
    public static final Item ENDRITE_BLOCK = registerBlockItem("endrite_block", ModBlocks.ENDRITE_BLOCK);
    public static final Item VOID_STONE = registerBlockItem("void_stone", ModBlocks.VOID_STONE);
    public static final Item VOID_STONE_BRICKS = registerBlockItem("void_stone_bricks", ModBlocks.VOID_STONE_BRICKS);
    public static final Item CRYSTAL_GLASS = registerBlockItem("crystal_glass", ModBlocks.CRYSTAL_GLASS);
    public static final Item VOID_LAMP = registerBlockItem("void_lamp", ModBlocks.VOID_LAMP);
    public static final Item VOID_RUNE_BLOCK = registerBlockItem("void_rune_block", ModBlocks.VOID_RUNE_BLOCK);

    public static final Item DRAGON_HEART = register("dragon_heart", new Item(new Item.Settings().maxCount(1)));
    public static final Item DRAGON_HEART_FRAGMENT = register("dragon_heart_fragment", new Item(new Item.Settings().maxCount(16)));
    public static final Item VOID_SCALE = register("void_scale", new Item(new Item.Settings()));
    public static final Item VOID_DUST = register("void_dust", new Item(new Item.Settings()));
    public static final Item ENDRITE_SHARD = register("endrite_shard", new Item(new Item.Settings()));
    public static final Item CRYSTAL_SHARD = register("crystal_shard", new Item(new Item.Settings()));
    public static final Item SHADOW_SCALE = register("shadow_scale", new Item(new Item.Settings()));
    public static final Item HEARTWOOD_CORE = register("heartwood_core", new Item(new Item.Settings().maxCount(16)));
    public static final Item GLOOM_ESSENCE = register("gloom_essence", new Item(new Item.Settings()));
    public static final Item WRAITH_FEATHER = register("wraith_feather", new Item(new Item.Settings()));
    public static final Item ANCIENT_RUNE_FRAGMENT = register("ancient_rune_fragment", new Item(new Item.Settings()));
    public static final Item NAVIGATION_CRYSTAL = register("navigation_crystal", new Item(new Item.Settings().maxCount(1)));
    public static final Item CAMOUFLAGE_MEMBRANE = register("camouflage_membrane", new Item(new Item.Settings()));
    public static final Item PARASITE_FLUID = register("parasite_fluid", new Item(new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(endplus.MOD_ID, name), item);
    }

    private static Item registerBlockItem(String name, Block block) {
        return register(name, new BlockItem(block, new Item.Settings()));
    }

    public static void initialize() {}
}
