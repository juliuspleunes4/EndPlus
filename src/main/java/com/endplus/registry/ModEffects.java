package com.endplus.registry;

import com.endplus.EndPlus;
import com.endplus.effect.VoidRotEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {

    public static RegistryEntry<StatusEffect> VOID_ROT;

    public static void initialize() {
        VOID_ROT = Registry.registerReference(
                Registries.STATUS_EFFECT,
                Identifier.of(EndPlus.MOD_ID, "void_rot"),
                new VoidRotEffect()
        );
    }
}
