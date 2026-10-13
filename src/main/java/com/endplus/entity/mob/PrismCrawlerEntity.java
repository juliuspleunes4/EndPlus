package com.endplus.entity.mob;

import com.endplus.entity.projectile.CrystalShardEntity;
import com.endplus.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Spider-like crawler of the Crystalline Fields (PLANNING 6.2). Climbs any surface, fires bursts of
 * three Crystal Shards at range, and when hurt shatters nearby crystal blocks, spraying shrapnel.
 */
public class PrismCrawlerEntity extends HostileEntity {

    private static final TrackedData<Byte> CLIMBING_FLAG =
            DataTracker.registerData(PrismCrawlerEntity.class, TrackedDataHandlerRegistry.BYTE);

    private static final int BURST_COOLDOWN = 70;
    private static final int CRACK_COOLDOWN = 20;
    private static final int CRACK_RADIUS = 4;
    private static final float SHRAPNEL_DAMAGE = 3.0f;

    private int burstCooldown;
    private int crackCooldown;

    public PrismCrawlerEntity(EntityType<? extends PrismCrawlerEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 8;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CLIMBING_FLAG, (byte) 0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SpiderNavigation(this, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new ShardBurstGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            return;
        }
        this.setClimbingWall(this.horizontalCollision);
        if (this.burstCooldown > 0) {
            this.burstCooldown--;
        }
        if (this.crackCooldown > 0) {
            this.crackCooldown--;
        }
    }

    @Override
    public boolean isClimbing() {
        return (this.dataTracker.get(CLIMBING_FLAG) & 1) != 0;
    }

    private void setClimbingWall(boolean climbing) {
        byte flags = this.dataTracker.get(CLIMBING_FLAG);
        flags = climbing ? (byte) (flags | 1) : (byte) (flags & ~1);
        this.dataTracker.set(CLIMBING_FLAG, flags);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean result = super.damage(source, amount);
        if (result && this.getWorld() instanceof ServerWorld world && this.crackCooldown == 0) {
            this.crackCooldown = CRACK_COOLDOWN;
            this.shatterNearbyCrystals(world);
        }
        return result;
    }

    /** Breaks up to two nearby crystal blocks; each sprays shrapnel that hurts anything but crawlers. */
    private void shatterNearbyCrystals(ServerWorld world) {
        int shattered = 0;
        for (int attempt = 0; attempt < 12 && shattered < 2; attempt++) {
            BlockPos pos = this.getBlockPos().add(
                    this.random.nextInt(CRACK_RADIUS * 2 + 1) - CRACK_RADIUS,
                    this.random.nextInt(CRACK_RADIUS + 1) - 1,
                    this.random.nextInt(CRACK_RADIUS * 2 + 1) - CRACK_RADIUS);
            BlockState state = world.getBlockState(pos);
            if (!state.isOf(ModBlocks.CRYSTAL_SPIRE) && !state.isOf(ModBlocks.CRYSTAL_GLASS)) {
                continue;
            }
            world.breakBlock(pos, false, this);
            Vec3d center = Vec3d.ofCenter(pos);
            world.spawnParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 12, 0.4, 0.4, 0.4, 0.08);
            for (LivingEntity victim : world.getEntitiesByClass(LivingEntity.class, new Box(pos).expand(2.0),
                    entity -> !(entity instanceof PrismCrawlerEntity))) {
                victim.damage(world.getDamageSources().magic(), SHRAPNEL_DAMAGE);
            }
            shattered++;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SPIDER_AMBIENT;
    }

    /** Fires three shards in a narrow spread from range, then closes in to melee. */
    private static class ShardBurstGoal extends Goal {
        private final PrismCrawlerEntity crawler;

        ShardBurstGoal(PrismCrawlerEntity crawler) {
            this.crawler = crawler;
            this.setControls(EnumSet.of(Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.crawler.getTarget();
            if (target == null || this.crawler.burstCooldown > 0) {
                return false;
            }
            double distSq = this.crawler.squaredDistanceTo(target);
            return distSq >= 16.0 && distSq <= 256.0 && this.crawler.canSee(target);
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            LivingEntity target = this.crawler.getTarget();
            if (target == null) {
                return;
            }
            this.crawler.getLookControl().lookAt(target, 30.0f, 30.0f);
            Vec3d origin = new Vec3d(this.crawler.getX(), this.crawler.getEyeY() - 0.2, this.crawler.getZ());
            Vec3d aim = new Vec3d(target.getX(), target.getBodyY(0.5), target.getZ()).subtract(origin).normalize();
            for (int i = -1; i <= 1; i++) {
                Vec3d dir = aim.rotateY(i * 0.12f);
                CrystalShardEntity shard = new CrystalShardEntity(this.crawler.getWorld(), this.crawler);
                Vec3d pos = origin.add(dir.multiply(0.8));
                shard.setPosition(pos.x, pos.y, pos.z);
                shard.setVelocity(dir.x, dir.y, dir.z, 1.4f, 0.0f);
                this.crawler.getWorld().spawnEntity(shard);
            }
            this.crawler.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
            this.crawler.burstCooldown = BURST_COOLDOWN;
        }
    }
}
