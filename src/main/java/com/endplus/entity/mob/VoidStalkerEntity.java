package com.endplus.entity.mob;

import com.endplus.EndPlus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

/**
 * Pack-hunting ambusher of the Void Wastes (PLANNING 6.1). While motionless and unprovoked it
 * camouflages as End Stone (the client skips rendering it unless the viewer is within
 * {@link #REVEAL_RANGE} blocks); it leaps at players for burst damage and alerts nearby Stalkers
 * whenever it attacks or is hurt.
 */
public class VoidStalkerEntity extends HostileEntity {

    public static final double REVEAL_RANGE = 8.0;
    private static final double AMBUSH_RANGE = 12.0;
    private static final double PACK_ALERT_RANGE = 16.0;
    private static final int STILL_TICKS_TO_CAMOUFLAGE = 40;
    private static final int REVEAL_TICKS_ON_HIT = 100;
    private static final int LEAP_COOLDOWN = 60;
    private static final double LEAP_BONUS_DAMAGE = 3.5;
    private static final Identifier LEAP_BONUS_ID = Identifier.of(EndPlus.MOD_ID, "stalker_leap_bonus");

    private static final TrackedData<Boolean> CAMOUFLAGED =
            DataTracker.registerData(VoidStalkerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int stillTicks;
    private int revealTicks;
    private int leapTicks;
    private int leapCooldown;

    public VoidStalkerEntity(EntityType<? extends VoidStalkerEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 45.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CAMOUFLAGED, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LeapAtPlayerGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.add(5, new WanderAroundGoal(this, 0.6, 400));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false,
                entity -> this.squaredDistanceTo(entity) <= AMBUSH_RANGE * AMBUSH_RANGE));
    }

    public boolean isCamouflaged() {
        return this.dataTracker.get(CAMOUFLAGED);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            return;
        }
        if (this.revealTicks > 0) {
            this.revealTicks--;
        }
        if (this.leapTicks > 0) {
            this.leapTicks--;
        }
        if (this.leapCooldown > 0) {
            this.leapCooldown--;
        }
        boolean idle = this.getTarget() == null && this.revealTicks == 0
                && this.isOnGround() && this.getNavigation().isIdle();
        this.stillTicks = idle ? this.stillTicks + 1 : 0;
        boolean camouflaged = this.stillTicks >= STILL_TICKS_TO_CAMOUFLAGE;
        if (camouflaged != this.isCamouflaged()) {
            this.dataTracker.set(CAMOUFLAGED, camouflaged);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean result = super.damage(source, amount);
        if (result && !this.getWorld().isClient()) {
            this.reveal();
            if (source.getAttacker() instanceof LivingEntity attacker) {
                this.alertPack(attacker);
            }
        }
        return result;
    }

    @Override
    public boolean tryAttack(Entity target) {
        EntityAttributeInstance attack = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        boolean burst = this.leapTicks > 0 && attack != null;
        if (burst) {
            attack.addTemporaryModifier(new EntityAttributeModifier(
                    LEAP_BONUS_ID, LEAP_BONUS_DAMAGE, EntityAttributeModifier.Operation.ADD_VALUE));
        }
        boolean hit;
        try {
            hit = super.tryAttack(target);
        } finally {
            if (burst) {
                attack.removeModifier(LEAP_BONUS_ID);
            }
        }
        if (hit) {
            this.leapTicks = 0;
            if (target instanceof LivingEntity living) {
                this.alertPack(living);
            }
        }
        return hit;
    }

    private void reveal() {
        this.revealTicks = REVEAL_TICKS_ON_HIT;
        this.stillTicks = 0;
        if (this.isCamouflaged()) {
            this.dataTracker.set(CAMOUFLAGED, false);
        }
    }

    private void alertPack(LivingEntity target) {
        if (!EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(target)) {
            return;
        }
        List<VoidStalkerEntity> pack = this.getWorld().getEntitiesByClass(VoidStalkerEntity.class,
                this.getBoundingBox().expand(PACK_ALERT_RANGE),
                other -> other != this && other.isAlive() && other.getTarget() == null);
        for (VoidStalkerEntity other : pack) {
            other.setTarget(target);
            other.reveal();
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMAN_DEATH;
    }

    /** Pounces from 3-8 blocks out; the next hit within the leap window carries bonus damage. */
    private static class LeapAtPlayerGoal extends Goal {
        private final VoidStalkerEntity stalker;

        LeapAtPlayerGoal(VoidStalkerEntity stalker) {
            this.stalker = stalker;
            this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.stalker.getTarget();
            if (target == null || !this.stalker.isOnGround() || this.stalker.leapCooldown > 0) {
                return false;
            }
            double distSq = this.stalker.squaredDistanceTo(target);
            return distSq >= 9.0 && distSq <= 64.0 && this.stalker.canSee(target);
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            LivingEntity target = this.stalker.getTarget();
            if (target == null) {
                return;
            }
            Vec3d dir = new Vec3d(target.getX() - this.stalker.getX(), 0, target.getZ() - this.stalker.getZ());
            if (dir.lengthSquared() < 1.0e-4) {
                return;
            }
            dir = dir.normalize().multiply(0.9);
            this.stalker.setVelocity(dir.x, 0.45, dir.z);
            this.stalker.leapTicks = 20;
            this.stalker.leapCooldown = LEAP_COOLDOWN;
        }
    }
}
