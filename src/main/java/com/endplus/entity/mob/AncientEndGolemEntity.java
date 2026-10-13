package com.endplus.entity.mob;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Slow, hard-hitting guardian of the Sunken Citadel (PLANNING 6.5). It patrols the area around where it
 * was placed, never chases anything further than {@link #LEASH} blocks from that home, shrugs off
 * arrows, and every 30 seconds can wind up a Void Slam: a shockwave that hurts and flings everything
 * within five blocks.
 */
public class AncientEndGolemEntity extends HostileEntity {

    /** How far from its home it will pursue a target, in blocks. */
    public static final int LEASH = 20;
    private static final int PATROL_RADIUS = 10;
    private static final int SLAM_COOLDOWN = 600;
    private static final int SLAM_WINDUP = 24;
    private static final double SLAM_RADIUS = 5.0;
    private static final float SLAM_DAMAGE = 8.0f;

    private static final TrackedData<Boolean> SLAMMING =
            DataTracker.registerData(AncientEndGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private BlockPos home;
    private int slamCooldown = 100;
    private int slamWindup;

    public AncientEndGolemEntity(EntityType<? extends AncientEndGolemEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 20;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SLAMMING, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new VoidSlamGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(4, new ReturnHomeGoal(this));
        this.goalSelector.add(5, new PatrolGoal(this));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false,
                entity -> this.isWithinLeash(entity.getBlockPos())));
    }

    public boolean isSlamming() {
        return this.dataTracker.get(SLAMMING);
    }

    private boolean isWithinLeash(BlockPos pos) {
        return this.home == null || this.home.getSquaredDistance(pos) <= (double) LEASH * LEASH;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            return;
        }
        if (this.home == null) {
            this.home = this.getBlockPos();
        }
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
        LivingEntity target = this.getTarget();
        if (target != null && (!this.isWithinLeash(target.getBlockPos())
                || this.home.getSquaredDistance(this.getBlockPos()) > (double) (LEASH + 2) * (LEASH + 2))) {
            // Never pursue past the leash: let go and walk back.
            this.setTarget(null);
            this.getNavigation().stop();
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.takeKnockback(1.2, this.getX() - living.getX(), this.getZ() - living.getZ());
        }
        return hit;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.isIn(DamageTypeTags.IS_PROJECTILE) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.home != null) {
            nbt.putInt("HomeX", this.home.getX());
            nbt.putInt("HomeY", this.home.getY());
            nbt.putInt("HomeZ", this.home.getZ());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("HomeX")) {
            this.home = new BlockPos(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ"));
        }
    }

    // -- Void Slam ------------------------------------------------------------------------------

    private void performSlam(ServerWorld world) {
        Box area = this.getBoundingBox().expand(SLAM_RADIUS, 2.5, SLAM_RADIUS);
        for (LivingEntity victim : world.getEntitiesByClass(LivingEntity.class, area,
                entity -> entity != this && !(entity instanceof AncientEndGolemEntity)
                        && entity.squaredDistanceTo(this.getX(), entity.getY(), this.getZ()) <= SLAM_RADIUS * SLAM_RADIUS)) {
            victim.damage(world.getDamageSources().mobAttack(this), SLAM_DAMAGE);
            victim.takeKnockback(1.0, this.getX() - victim.getX(), this.getZ() - victim.getZ());
            victim.addVelocity(0.0, 0.45, 0.0);
            victim.velocityModified = true;
        }
        BlockStateParticleEffect debris = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.PURPUR_BLOCK.getDefaultState());
        for (int ring = 1; ring <= 5; ring++) {
            int points = 8 + ring * 4;
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2.0 * i / points;
                double px = this.getX() + Math.cos(angle) * ring;
                double pz = this.getZ() + Math.sin(angle) * ring;
                world.spawnParticles(debris, px, this.getY() + 0.1, pz, 2, 0.15, 0.1, 0.15, 0.05);
                if (ring % 2 == 1) {
                    world.spawnParticles(ParticleTypes.REVERSE_PORTAL, px, this.getY() + 0.3, pz, 1, 0.1, 0.3, 0.1, 0.02);
                }
            }
        }
        world.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0, 0, 0, 0);
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                this.getSoundCategory(), 0.7f, 0.6f);
        world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK,
                this.getSoundCategory(), 1.0f, 0.5f);
    }

    /** Raises both fists, then brings them down in a shockwave. Used once every 30 seconds. */
    private static class VoidSlamGoal extends Goal {
        private final AncientEndGolemEntity golem;

        VoidSlamGoal(AncientEndGolemEntity golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.golem.getTarget();
            return target != null && this.golem.slamCooldown == 0
                    && this.golem.squaredDistanceTo(target) <= 20.0 && this.golem.canSee(target);
        }

        @Override
        public boolean shouldContinue() {
            return this.golem.slamWindup > 0;
        }

        @Override
        public void start() {
            this.golem.slamWindup = SLAM_WINDUP;
            this.golem.getNavigation().stop();
            this.golem.dataTracker.set(SLAMMING, true);
            this.golem.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 0.5f);
        }

        @Override
        public void tick() {
            LivingEntity target = this.golem.getTarget();
            if (target != null) {
                this.golem.getLookControl().lookAt(target, 30.0f, 30.0f);
            }
            if (--this.golem.slamWindup <= 0 && this.golem.getWorld() instanceof ServerWorld world) {
                this.golem.performSlam(world);
            }
        }

        @Override
        public void stop() {
            this.golem.slamWindup = 0;
            this.golem.slamCooldown = SLAM_COOLDOWN;
            this.golem.dataTracker.set(SLAMMING, false);
        }
    }

    // -- patrolling -----------------------------------------------------------------------------

    /** Walks back towards home when it has drifted too far and has nothing to fight. */
    private static class ReturnHomeGoal extends Goal {
        private final AncientEndGolemEntity golem;

        ReturnHomeGoal(AncientEndGolemEntity golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return this.golem.getTarget() == null && this.golem.home != null
                    && this.golem.home.getSquaredDistance(this.golem.getBlockPos()) > (double) PATROL_RADIUS * PATROL_RADIUS * 2.25;
        }

        @Override
        public boolean shouldContinue() {
            return this.golem.getTarget() == null && !this.golem.getNavigation().isIdle();
        }

        @Override
        public void start() {
            this.golem.getNavigation().startMovingTo(
                    this.golem.home.getX() + 0.5, this.golem.home.getY(), this.golem.home.getZ() + 0.5, 0.8);
        }
    }

    /** Slow rounds of the area around home, pausing between legs. */
    private static class PatrolGoal extends Goal {
        private final AncientEndGolemEntity golem;

        PatrolGoal(AncientEndGolemEntity golem) {
            this.golem = golem;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return this.golem.getTarget() == null && this.golem.home != null
                    && this.golem.getRandom().nextInt(80) == 0;
        }

        @Override
        public boolean shouldContinue() {
            return this.golem.getTarget() == null && !this.golem.getNavigation().isIdle();
        }

        @Override
        public void start() {
            BlockPos home = this.golem.home;
            double x = home.getX() + 0.5 + (this.golem.getRandom().nextDouble() * 2.0 - 1.0) * PATROL_RADIUS;
            double z = home.getZ() + 0.5 + (this.golem.getRandom().nextDouble() * 2.0 - 1.0) * PATROL_RADIUS;
            this.golem.getNavigation().startMovingTo(x, home.getY(), z, 0.5);
        }
    }

    // -- sounds ---------------------------------------------------------------------------------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.block.BlockState state) {
        this.playSound(SoundEvents.ENTITY_IRON_GOLEM_STEP, 1.0f, 0.7f);
    }
}
