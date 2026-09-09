package com.endplus.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/**
 * Dive-attack goal for End+ flyers. The entity hangs back until its swoop timer elapses, then
 * accelerates straight at the target's eyes; contact triggers {@link MobEntity#tryAttack} so
 * per-entity on-hit effects (levitation, breath) stay in the entity class.
 */
public class FlyingSwoopGoal extends Goal {

    private final MobEntity mob;
    private final int swoopInterval;
    private int cooldown;

    public FlyingSwoopGoal(MobEntity mob, int swoopInterval) {
        this.mob = mob;
        this.swoopInterval = swoopInterval;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || this.mob.getMoveControl().isMoving()) {
            return false;
        }
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        return this.mob.squaredDistanceTo(target) > 9.0;
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && this.mob.getMoveControl().isMoving();
    }

    @Override
    public void start() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            Vec3d eyes = target.getEyePos();
            this.mob.getMoveControl().moveTo(eyes.x, eyes.y, eyes.z, 1.2);
        }
    }

    @Override
    public void stop() {
        this.cooldown = this.swoopInterval;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        if (this.mob.getBoundingBox().expand(0.3).intersects(target.getBoundingBox())) {
            this.mob.tryAttack(target);
            this.mob.getMoveControl().moveTo(this.mob.getX(), this.mob.getY() + 6.0, this.mob.getZ(), 1.0);
        } else {
            Vec3d eyes = target.getEyePos();
            this.mob.getMoveControl().moveTo(eyes.x, eyes.y, eyes.z, 1.2);
        }
    }
}
