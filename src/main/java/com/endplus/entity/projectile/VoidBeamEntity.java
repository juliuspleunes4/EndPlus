package com.endplus.entity.projectile;

import com.endplus.registry.ModEffects;
import com.endplus.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VoidBeamEntity extends AbstractFireballEntity {

    public VoidBeamEntity(EntityType<? extends VoidBeamEntity> type, World world) {
        super(type, world);
    }

    public VoidBeamEntity(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ) {
        super(ModEntities.VOID_BEAM, owner, velocityX, velocityY, velocityZ, world);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (this.getWorld().isClient()) return;
        Entity target = entityHitResult.getEntity();
        if (target instanceof LivingEntity living && this.getWorld() instanceof ServerWorld serverWorld) {
            living.damage(serverWorld, serverWorld.getDamageSources().magic(), 12.0f);
            Vec3d knockback = this.getVelocity().normalize().multiply(1.5);
            living.addVelocity(knockback.x, 0.3, knockback.z);
            living.addStatusEffect(new StatusEffectInstance(ModEffects.VOID_ROT, 100, 0));
        }
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!this.getWorld().isClient()) {
            this.discard();
        }
    }

    @Override
    protected boolean isBurning() {
        return false;
    }
}
