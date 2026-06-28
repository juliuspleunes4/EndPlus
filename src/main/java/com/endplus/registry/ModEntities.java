package com.endplus.registry;

import com.endplus.EndPlus;
import com.endplus.entity.projectile.VoidBeamEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static EntityType<VoidBeamEntity> VOID_BEAM;

    public static void initialize() {
        VOID_BEAM = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(EndPlus.MOD_ID, "void_beam"),
                EntityType.Builder.<VoidBeamEntity>create(VoidBeamEntity::new, SpawnGroup.MISC)
                        .dimensions(0.3125f, 0.3125f)
                        .maxTrackingRange(4)
                        .trackingTickInterval(10)
                        .build()
        );
    }
}
