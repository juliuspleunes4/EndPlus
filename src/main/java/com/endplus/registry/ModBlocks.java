package com.endplus.registry;

import com.endplus.EndPlus;
import com.endplus.block.ModOreBlock;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class ModBlocks {

    public static final Block CRACKED_END_STONE = register("cracked_end_stone",
            new Block(AbstractBlock.Settings.copy(Blocks.END_STONE).strength(2.0f, 4.0f)));

    public static final Block VOID_TOUCHED_PURPUR = register("void_touched_purpur",
            new Block(AbstractBlock.Settings.copy(Blocks.PURPUR_BLOCK).mapColor(net.minecraft.block.MapColor.PURPLE)));

    public static final Block UMBRAL_LOG = register("umbral_log",
            new PillarBlock(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.BLACK)
                    .instrument(net.minecraft.block.enums.NoteBlockInstrument.BASS)
                    .strength(2.0f)
                    .sounds(BlockSoundGroup.WOOD)
                    .burnable()));

    public static final Block UMBRAL_WOOD = register("umbral_wood",
            new PillarBlock(AbstractBlock.Settings.copy(UMBRAL_LOG)));

    public static final Block STRIPPED_UMBRAL_LOG = register("stripped_umbral_log",
            new PillarBlock(AbstractBlock.Settings.copy(UMBRAL_LOG)));

    public static final Block STRIPPED_UMBRAL_WOOD = register("stripped_umbral_wood",
            new PillarBlock(AbstractBlock.Settings.copy(UMBRAL_LOG)));

    public static final Block UMBRAL_PLANKS = register("umbral_planks",
            new Block(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.BLACK)
                    .instrument(net.minecraft.block.enums.NoteBlockInstrument.BASS)
                    .strength(2.0f, 3.0f)
                    .sounds(BlockSoundGroup.WOOD)
                    .burnable()));

    public static final Block UMBRAL_SLAB = register("umbral_slab",
            new SlabBlock(AbstractBlock.Settings.copy(UMBRAL_PLANKS)));

    public static final Block UMBRAL_STAIRS = register("umbral_stairs",
            new StairsBlock(UMBRAL_PLANKS.getDefaultState(), AbstractBlock.Settings.copy(UMBRAL_PLANKS)));

    public static final Block UMBRAL_LEAVES = register("umbral_leaves",
            new LeavesBlock(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.DARK_DULL_PINK)
                    .strength(0.2f)
                    .ticksRandomly()
                    .sounds(BlockSoundGroup.GRASS)
                    .nonOpaque()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)
                    .burnable()
                    .pistonBehavior(net.minecraft.block.piston.PistonBehavior.DESTROY)));

    public static final Block VOIDMOSS = register("voidmoss",
            new Block(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.BLUE)
                    .strength(0.4f)
                    .sounds(BlockSoundGroup.MOSS)
                    .noCollision()
                    .pistonBehavior(net.minecraft.block.piston.PistonBehavior.DESTROY)));

    public static final Block CRYSTAL_SPIRE = register("crystal_spire",
            new Block(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.CYAN)
                    .strength(0.3f)
                    .sounds(BlockSoundGroup.GLASS)
                    .luminance(state -> 8)
                    .nonOpaque()
                    .pistonBehavior(net.minecraft.block.piston.PistonBehavior.DESTROY)));

    public static final Block ENDRITE_ORE = register("endrite_ore",
            new ModOreBlock(UniformIntProvider.create(2, 5),
                    AbstractBlock.Settings.copy(Blocks.END_STONE)
                            .strength(4.0f, 6.0f)
                            .requiresTool()));

    public static final Block VOID_ORE = register("void_ore",
            new ModOreBlock(UniformIntProvider.create(1, 3),
                    AbstractBlock.Settings.copy(Blocks.END_STONE)
                            .strength(3.0f, 5.0f)
                            .requiresTool()));

    public static final Block ENDRITE_BLOCK = register("endrite_block",
            new Block(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.PURPLE)
                    .strength(6.0f, 8.0f)
                    .sounds(BlockSoundGroup.METAL)
                    .requiresTool()));

    public static final Block VOID_STONE = register("void_stone",
            new Block(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.BLACK)
                    .strength(3.0f, 6.0f)
                    .sounds(BlockSoundGroup.STONE)
                    .requiresTool()));

    public static final Block VOID_STONE_BRICKS = register("void_stone_bricks",
            new Block(AbstractBlock.Settings.copy(VOID_STONE)));

    public static final Block CRYSTAL_GLASS = register("crystal_glass",
            new Block(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.CYAN)
                    .strength(0.3f)
                    .sounds(BlockSoundGroup.GLASS)
                    .luminance(state -> 8)
                    .nonOpaque()
                    .allowsSpawning((state, world, pos, type) -> false)));

    public static final Block VOID_LAMP = register("void_lamp",
            new Block(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.BLACK)
                    .strength(0.3f)
                    .sounds(BlockSoundGroup.GLASS)
                    .luminance(state -> 15)));

    public static final Block VOID_RUNE_BLOCK = register("void_rune_block",
            new Block(AbstractBlock.Settings.create()
                    .mapColor(net.minecraft.block.MapColor.PURPLE)
                    .strength(3.0f, 9.0f)
                    .sounds(BlockSoundGroup.STONE)
                    .luminance(state -> 4)
                    .requiresTool()));

    private static Block register(String name, Block block) {
        return Registry.register(Registries.BLOCK, Identifier.of(EndPlus.MOD_ID, name), block);
    }

    public static void initialize() {}
}
