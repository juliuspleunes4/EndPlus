package com.endplus.registry;

import com.endplus.EndPlus;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModCreativeTabs {

    public static final ItemGroup endplus_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(endplus.MOD_ID, "main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.DRAGON_HEART))
                    .displayName(Text.translatable("itemGroup.endplus.main"))
                    .entries((context, entries) -> {
                        entries.add(ModItems.CRACKED_END_STONE);
                        entries.add(ModItems.VOID_TOUCHED_PURPUR);

                        entries.add(ModItems.UMBRAL_LOG);
                        entries.add(ModItems.UMBRAL_WOOD);
                        entries.add(ModItems.STRIPPED_UMBRAL_LOG);
                        entries.add(ModItems.STRIPPED_UMBRAL_WOOD);
                        entries.add(ModItems.UMBRAL_PLANKS);
                        entries.add(ModItems.UMBRAL_STAIRS);
                        entries.add(ModItems.UMBRAL_SLAB);
                        entries.add(ModItems.UMBRAL_LEAVES);
                        entries.add(ModItems.VOIDMOSS);

                        entries.add(ModItems.CRYSTAL_SPIRE);
                        entries.add(ModItems.CRYSTAL_GLASS);

                        entries.add(ModItems.ENDRITE_ORE);
                        entries.add(ModItems.VOID_ORE);
                        entries.add(ModItems.ENDRITE_BLOCK);
                        entries.add(ModItems.VOID_STONE);
                        entries.add(ModItems.VOID_STONE_BRICKS);
                        entries.add(ModItems.VOID_LAMP);
                        entries.add(ModItems.VOID_RUNE_BLOCK);

                        entries.add(ModItems.DRAGON_HEART);
                        entries.add(ModItems.DRAGON_HEART_FRAGMENT);
                        entries.add(ModItems.VOID_SCALE);
                        entries.add(ModItems.VOID_DUST);
                        entries.add(ModItems.ENDRITE_SHARD);
                        entries.add(ModItems.CRYSTAL_SHARD);
                        entries.add(ModItems.SHADOW_SCALE);
                        entries.add(ModItems.HEARTWOOD_CORE);
                        entries.add(ModItems.GLOOM_ESSENCE);
                        entries.add(ModItems.WRAITH_FEATHER);
                        entries.add(ModItems.ANCIENT_RUNE_FRAGMENT);
                        entries.add(ModItems.NAVIGATION_CRYSTAL);
                        entries.add(ModItems.CAMOUFLAGE_MEMBRANE);
                        entries.add(ModItems.PARASITE_FLUID);
                    })
                    .build()
    );

    public static void initialize() {}
}
