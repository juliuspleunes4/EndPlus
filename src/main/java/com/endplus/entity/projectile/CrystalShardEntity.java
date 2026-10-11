package com.endplus.entity.projectile;

import com.endplus.registry.ModEntities;
import com.endplus.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** Light, fast shard fired by Prism Crawlers; 2 damage per hit. */
public class CrystalShardEntity extends ThrownItemEntity {

    private static final float DAMAGE = 2.0f;
    private static final int MAX_AGE = 60;

    public CrystalShardEntity(EntityType<? extends CrystalShardEntity> type, World world) {
        super(type, world);
    }

    public CrystalShardEntity(World world, LivingEntity owner) {
        super(ModEntities.CRYSTAL_SHARD, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CRYSTAL_SHARD;
    }

    @Override
    protected double getGravity() {
        return 0.015;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient() && this.age > MAX_AGE) {
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity target = entityHitResult.getEntity();
        target.damage(this.getDamageSources().thrown(this, this.getOwner()), DAMAGE);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, this.getStack()),
                    this.getX(), this.getY(), this.getZ(), 6, 0.1, 0.1, 0.1, 0.05);
            this.discard();
        }
    }
}
