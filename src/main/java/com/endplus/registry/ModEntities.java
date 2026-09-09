package com.endplus.registry;

import com.endplus.EndPlus;
import com.endplus.entity.minion.*;
import com.endplus.entity.projectile.VoidBeamEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static EntityType<VoidBeamEntity> VOID_BEAM;

    public static EntityType<VoidImpEntity> VOID_IMP;
    public static EntityType<EnderPhantomEntity> ENDER_PHANTOM;
    public static EntityType<EndriteGolemEntity> ENDRITE_GOLEM;
    public static EntityType<VoidWitchEntity> VOID_WITCH;
    public static EntityType<ShadowDrakeEntity> SHADOW_DRAKE;

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

        VOID_IMP = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(EndPlus.MOD_ID, "void_imp"),
                EntityType.Builder.<VoidImpEntity>create(VoidImpEntity::new, SpawnGroup.MONSTER)
                        .dimensions(0.6f, 1.2f)
                        .maxTrackingRange(8)
                        .build()
        );

        ENDER_PHANTOM = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(EndPlus.MOD_ID, "ender_phantom"),
                EntityType.Builder.<EnderPhantomEntity>create(EnderPhantomEntity::new, SpawnGroup.MONSTER)
                        .dimensions(0.9f, 0.5f)
                        .maxTrackingRange(8)
                        .build()
        );

        ENDRITE_GOLEM = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(EndPlus.MOD_ID, "endrite_golem"),
                EntityType.Builder.<EndriteGolemEntity>create(EndriteGolemEntity::new, SpawnGroup.MONSTER)
                        .dimensions(1.4f, 2.7f)
                        .maxTrackingRange(10)
                        .build()
        );

        VOID_WITCH = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(EndPlus.MOD_ID, "void_witch"),
                EntityType.Builder.<VoidWitchEntity>create(VoidWitchEntity::new, SpawnGroup.MONSTER)
                        .dimensions(0.6f, 1.95f)
                        .maxTrackingRange(8)
                        .build()
        );

        SHADOW_DRAKE = Registry.register(
                Registries.ENTITY_TYPE,
                Identifier.of(EndPlus.MOD_ID, "shadow_drake"),
                EntityType.Builder.<ShadowDrakeEntity>create(ShadowDrakeEntity::new, SpawnGroup.MONSTER)
                        .dimensions(1.0f, 0.8f)
                        .maxTrackingRange(10)
                        .build()
        );
    }
}
