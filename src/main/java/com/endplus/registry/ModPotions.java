package com.endplus.registry;

import com.endplus.EndPlus;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Resistance potions, which vanilla lacks, brewed from Parasite Fluid. All three variants share the
 * "resistance" base name so they reuse one set of translation keys.
 */
public class ModPotions {

    private static final int RESISTANCE_TICKS = 3 * 60 * 20;
    private static final int LONG_RESISTANCE_TICKS = 8 * 60 * 20;
    private static final int STRONG_RESISTANCE_TICKS = 90 * 20;

    public static RegistryEntry<Potion> RESISTANCE;
    public static RegistryEntry<Potion> LONG_RESISTANCE;
    public static RegistryEntry<Potion> STRONG_RESISTANCE;

    public static void initialize() {
        RESISTANCE = register("resistance",
                new Potion("resistance", new StatusEffectInstance(StatusEffects.RESISTANCE, RESISTANCE_TICKS)));
        LONG_RESISTANCE = register("long_resistance",
                new Potion("resistance", new StatusEffectInstance(StatusEffects.RESISTANCE, LONG_RESISTANCE_TICKS)));
        STRONG_RESISTANCE = register("strong_resistance",
                new Potion("resistance", new StatusEffectInstance(StatusEffects.RESISTANCE, STRONG_RESISTANCE_TICKS, 1)));

        // Splash and lingering variants come for free from vanilla's gunpowder and dragon's breath recipes.
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.AWKWARD, ModItems.PARASITE_FLUID, RESISTANCE);
            builder.registerPotionRecipe(RESISTANCE, Items.REDSTONE, LONG_RESISTANCE);
            builder.registerPotionRecipe(RESISTANCE, Items.GLOWSTONE_DUST, STRONG_RESISTANCE);
        });
    }

    private static RegistryEntry<Potion> register(String name, Potion potion) {
        return Registry.registerReference(Registries.POTION, Identifier.of(EndPlus.MOD_ID, name), potion);
    }
}
