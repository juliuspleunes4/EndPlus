package com.endplus.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

/**
 * Idle/repositioning movement for End+ flyers. When a target exists the entity picks points above
 * and around it (staying airborne, out of melee); otherwise it drifts near its current position.
 */
public class FlyingCircleGoal extends Goal {

    private final MobEntity mob;

    public FlyingCircleGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return !this.mob.getMoveControl().isMoving() && this.mob.getRandom().nextInt(toGoalTicks(6)) == 0;
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }

    @Override
    public void start() {
        LivingEntity target = this.mob.getTarget();
        BlockPos anchor = target != null ? target.getBlockPos().up(6) : this.mob.getBlockPos();
        for (int attempt = 0; attempt < 3; attempt++) {
            BlockPos candidate = anchor.add(
                    this.mob.getRandom().nextInt(15) - 7,
                    this.mob.getRandom().nextInt(7) - 2,
                    this.mob.getRandom().nextInt(15) - 7);
            if (this.mob.getWorld().isAir(candidate)) {
                this.mob.getMoveControl().moveTo(candidate.getX() + 0.5, candidate.getY() + 0.5, candidate.getZ() + 0.5, 0.6);
                if (target == null) {
                    this.mob.getLookControl().lookAt(candidate.getX() + 0.5, candidate.getY() + 0.5, candidate.getZ() + 0.5, 180.0F, 20.0F);
                }
                return;
            }
        }
    }
}
