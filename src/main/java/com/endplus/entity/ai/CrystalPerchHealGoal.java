package com.endplus.entity.ai;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

/**
 * PLANNING.md §3.5 — the Shadow Drake perches on a standing End Crystal and channels its energy
 * into the Ender Dragon, healing it. Players have to destroy the crystal it is sitting on to stop
 * the channel. Uses vanilla {@link EndCrystalEntity} directly, so any live crystal (pillar or
 * Void Shield) is a valid perch.
 */
public class CrystalPerchHealGoal extends Goal {

    private static final double SEARCH_RADIUS = 48.0;
    private static final float HEAL_PER_SECOND = 5.0f;

    private final MobEntity drake;
    private EndCrystalEntity perch;
    private EnderDragonEntity dragon;
    private int rescanCooldown;
    private int healTimer;

    public CrystalPerchHealGoal(MobEntity drake) {
        this.drake = drake;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.rescanCooldown > 0) {
            this.rescanCooldown--;
            return false;
        }
        this.rescanCooldown = 40;
        this.dragon = this.findDragon();
        if (this.dragon == null || this.dragon.getHealth() >= this.dragon.getMaxHealth()) {
            return false;
        }
        this.perch = this.findNearestCrystal();
        return this.perch != null;
    }

    @Override
    public boolean shouldContinue() {
        return this.perch != null && this.perch.isAlive()
                && this.dragon != null && this.dragon.isAlive()
                && this.dragon.getHealth() < this.dragon.getMaxHealth();
    }

    @Override
    public void start() {
        this.moveToPerch();
    }

    @Override
    public void stop() {
        this.perch = null;
        this.dragon = null;
        this.healTimer = 0;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.perch == null || this.dragon == null) {
            return;
        }
        this.moveToPerch();
        double top = this.perch.getY() + 1.2;
        if (this.drake.squaredDistanceTo(this.perch.getX(), top, this.perch.getZ()) < 4.0) {
            if (++this.healTimer >= 20) {
                this.healTimer = 0;
                this.dragon.heal(HEAL_PER_SECOND);
            }
        }
    }

    private void moveToPerch() {
        this.drake.getMoveControl().moveTo(this.perch.getX(), this.perch.getY() + 1.2, this.perch.getZ(), 1.1);
    }

    private EnderDragonEntity findDragon() {
        Box box = this.drake.getBoundingBox().expand(128.0);
        List<EnderDragonEntity> found = this.drake.getWorld().getEntitiesByClass(EnderDragonEntity.class, box, e -> e.isAlive());
        return found.isEmpty() ? null : found.get(0);
    }

    private EndCrystalEntity findNearestCrystal() {
        Box box = this.drake.getBoundingBox().expand(SEARCH_RADIUS);
        EndCrystalEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (EndCrystalEntity crystal : this.drake.getWorld().getEntitiesByClass(EndCrystalEntity.class, box, e -> e.isAlive())) {
            double d = this.drake.squaredDistanceTo(crystal);
            if (d < bestDist) {
                bestDist = d;
                best = crystal;
            }
        }
        return best;
    }
}
