package com.endplus.entity.minion;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class VoidImpEntity extends HostileEntity {

    private int blinkCooldown;

    public VoidImpEntity(EntityType<? extends VoidImpEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 3;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.4, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this).setGroupRevenge());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.blinkCooldown > 0) {
            this.blinkCooldown--;
        }
        if (this.getWorld().isClient()) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (this.blinkCooldown == 0 && target != null && this.squaredDistanceTo(target) > 36.0
                && this.getNavigation().isIdle()) {
            this.blinkTowards(target);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean result = super.damage(source, amount);
        if (result && !this.getWorld().isClient() && this.getRandom().nextFloat() < 0.3f) {
            this.blinkAway();
        }
        return result;
    }

    private void blinkTowards(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0e-4) {
            return;
        }
        double step = 3.0 + this.getRandom().nextDouble() * 2.0;
        double x = this.getX() + dx / length * step;
        double z = this.getZ() + dz / length * step;
        if (this.teleport(x, target.getY(), z, true)) {
            this.blinkCooldown = 40;
        }
    }

    private void blinkAway() {
        for (int i = 0; i < 16; i++) {
            double x = this.getX() + (this.getRandom().nextDouble() - 0.5) * 8.0;
            double y = this.getY() + (this.getRandom().nextDouble() - 0.5) * 4.0;
            double z = this.getZ() + (this.getRandom().nextDouble() - 0.5) * 8.0;
            if (this.teleport(x, y, z, true)) {
                break;
            }
        }
    }
}
