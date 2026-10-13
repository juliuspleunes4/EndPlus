package com.endplus.entity.mob;

import com.endplus.registry.ModBlocks;
import net.minecraft.block.BlockState;
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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Patient ambush predator of the Umbral Forest (PLANNING 6.3). Near-invisible in the dark (light
 * level below {@link #SHADOW_LIGHT_LEVEL}), it shadows its target from a distance, then lunges for a
 * single devastating strike that inflicts Gloom (Slowness II + Blindness) before pulling back to stalk
 * again. It phases through Umbral Wood, so wooden walls do not stop it.
 */
public class ShadeLurkerEntity extends HostileEntity {

    private static final int SHADOW_LIGHT_LEVEL = 4;
    private static final int GLOOM_TICKS = 60;
    private static final int LUNGE_TICKS = 80;
    private static final float FADE_STEP = 0.05f;
    private static final double STALK_MIN_DISTANCE = 9.0;
    private static final double STALK_MAX_DISTANCE = 13.0;

    private static final TrackedData<Boolean> SHADOWED =
            DataTracker.registerData(ShadeLurkerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private boolean lunging;
    private int lungeTicks;
    private int stalkTicks = randomStalkTicks();
    private boolean phasing;
    private float shadowProgress;
    private float prevShadowProgress;

    public ShadeLurkerEntity(EntityType<? extends ShadeLurkerEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 12;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 55.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 28.0);
    }

    private int randomStalkTicks() {
        return 100 + this.random.nextInt(80);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SHADOWED, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new LungeGoal(this));
        this.goalSelector.add(3, new StalkGoal(this));
        this.goalSelector.add(5, new WanderAroundGoal(this, 0.7));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    public boolean isShadowed() {
        return this.dataTracker.get(SHADOWED);
    }

    /** Client-side fade 0 (lit, fully visible) .. 1 (in shadow, nearly invisible). */
    public float getShadowProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevShadowProgress, this.shadowProgress);
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient()) {
            this.updatePhasing();
        }
        super.tick();
        if (this.getWorld().isClient()) {
            this.prevShadowProgress = this.shadowProgress;
            float goal = this.isShadowed() ? 1.0f : 0.0f;
            this.shadowProgress += MathHelper.clamp(goal - this.shadowProgress, -FADE_STEP, FADE_STEP);
            return;
        }
        if (this.age % 10 == 0) {
            boolean shadowed = this.getWorld().getLightLevel(this.getBlockPos()) < SHADOW_LIGHT_LEVEL;
            if (shadowed != this.isShadowed()) {
                this.dataTracker.set(SHADOWED, shadowed);
            }
        }
        this.tickHunt();
    }

    private void tickHunt() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            this.lunging = false;
            this.stalkTicks = this.randomStalkTicks();
            return;
        }
        if (this.lunging) {
            if (--this.lungeTicks <= 0) {
                this.endLunge();
            } else if (this.getNavigation().isIdle() && this.umbralBetween(target)) {
                // No path (wood wall in the way): push straight through it.
                this.getMoveControl().moveTo(target.getX(), target.getY(), target.getZ(), 1.4);
            }
        } else if (--this.stalkTicks <= 0) {
            this.startLunge(target);
        }
    }

    private void startLunge(LivingEntity target) {
        this.lunging = true;
        this.lungeTicks = LUNGE_TICKS;
        if (this.isOnGround()) {
            Vec3d dir = new Vec3d(target.getX() - this.getX(), 0, target.getZ() - this.getZ());
            if (dir.lengthSquared() > 1.0e-4) {
                dir = dir.normalize().multiply(0.6);
                this.setVelocity(dir.x, 0.3, dir.z);
            }
        }
    }

    private void endLunge() {
        this.lunging = false;
        this.stalkTicks = this.randomStalkTicks();
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit) {
            if (target instanceof LivingEntity living) {
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, GLOOM_TICKS, 1), this);
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, GLOOM_TICKS, 0), this);
            }
            this.endLunge();
        }
        return hit;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean result = super.damage(source, amount);
        if (result && !this.getWorld().isClient() && !this.lunging) {
            // Provoked: stop circling and strike back soon.
            this.stalkTicks = Math.min(this.stalkTicks, 20);
        }
        return result;
    }

    // -- phasing through Umbral Wood ------------------------------------------------------------

    private static boolean isUmbralWood(BlockState state) {
        return state.isOf(ModBlocks.UMBRAL_LOG) || state.isOf(ModBlocks.UMBRAL_WOOD)
                || state.isOf(ModBlocks.STRIPPED_UMBRAL_LOG) || state.isOf(ModBlocks.STRIPPED_UMBRAL_WOOD)
                || state.isOf(ModBlocks.UMBRAL_PLANKS) || state.isOf(ModBlocks.UMBRAL_SLAB)
                || state.isOf(ModBlocks.UMBRAL_STAIRS);
    }

    /** While touching Umbral Wood, ignore block collision (and gravity, so it doesn't sink out of the floor). */
    private void updatePhasing() {
        boolean touching = false;
        Box box = this.getBoundingBox().expand(0.3);
        for (BlockPos pos : BlockPos.iterate(BlockPos.ofFloored(box.minX, box.minY, box.minZ), BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ))) {
            if (isUmbralWood(this.getWorld().getBlockState(pos))) {
                touching = true;
                break;
            }
        }
        if (touching != this.phasing) {
            this.phasing = touching;
            this.noClip = touching;
            this.setNoGravity(touching);
        }
    }

    /** True if Umbral Wood lies on the straight line towards the target within a few blocks. */
    private boolean umbralBetween(LivingEntity target) {
        Vec3d dir = new Vec3d(target.getX() - this.getX(), 0, target.getZ() - this.getZ());
        if (dir.lengthSquared() < 1.0e-4) {
            return false;
        }
        dir = dir.normalize();
        for (int step = 1; step <= 4; step++) {
            BlockPos feet = BlockPos.ofFloored(this.getX() + dir.x * step, this.getY(), this.getZ() + dir.z * step);
            if (isUmbralWood(this.getWorld().getBlockState(feet)) || isUmbralWood(this.getWorld().getBlockState(feet.up()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PHANTOM_DEATH;
    }

    // -- goals ----------------------------------------------------------------------------------

    /** Shadows the target at a distance, backing off if it gets too close. */
    private static class StalkGoal extends Goal {
        private final ShadeLurkerEntity lurker;

        StalkGoal(ShadeLurkerEntity lurker) {
            this.lurker = lurker;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return this.lurker.getTarget() != null && !this.lurker.lunging;
        }

        @Override
        public boolean shouldContinue() {
            return this.canStart();
        }

        @Override
        public void tick() {
            LivingEntity target = this.lurker.getTarget();
            if (target == null) {
                return;
            }
            this.lurker.getLookControl().lookAt(target, 30.0f, 30.0f);
            double dist = this.lurker.distanceTo(target);
            if (dist < STALK_MIN_DISTANCE) {
                Vec3d away = this.lurker.getPos().subtract(target.getPos()).multiply(1, 0, 1);
                if (away.lengthSquared() > 1.0e-4) {
                    Vec3d goal = this.lurker.getPos().add(away.normalize().multiply(6.0));
                    this.lurker.getNavigation().startMovingTo(goal.x, goal.y, goal.z, 1.0);
                }
            } else if (dist > STALK_MAX_DISTANCE) {
                this.lurker.getNavigation().startMovingTo(target, 0.9);
            } else {
                this.lurker.getNavigation().stop();
            }
        }
    }

    /** Melee charge, only active while a lunge is in progress. */
    private static class LungeGoal extends MeleeAttackGoal {
        private final ShadeLurkerEntity lurker;

        LungeGoal(ShadeLurkerEntity lurker) {
            super(lurker, 1.7, true);
            this.lurker = lurker;
        }

        @Override
        public boolean canStart() {
            return this.lurker.lunging && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return this.lurker.lunging && super.shouldContinue();
        }
    }
}
