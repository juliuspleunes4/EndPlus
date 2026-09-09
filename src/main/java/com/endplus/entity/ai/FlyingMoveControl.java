package com.endplus.entity.ai;

import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Velocity-based hover control for End+ flyers. Modelled on vanilla {@code VexEntity.VexMoveControl}:
 * the entity is nudged toward the move target every tick and yaw is aimed at the current combat
 * target when one exists, so swoop attacks read as deliberate dives rather than drift.
 */
public class FlyingMoveControl extends MoveControl {

    private final float speedTuning;

    public FlyingMoveControl(MobEntity entity, float speedTuning) {
        super(entity);
        this.speedTuning = speedTuning;
    }

    @Override
    public void tick() {
        if (this.state != MoveControl.State.MOVE_TO) {
            return;
        }
        Vec3d toTarget = new Vec3d(this.targetX - this.entity.getX(), this.targetY - this.entity.getY(), this.targetZ - this.entity.getZ());
        double distance = toTarget.length();
        if (distance < this.entity.getBoundingBox().getAverageSideLength()) {
            this.state = MoveControl.State.WAIT;
            this.entity.setVelocity(this.entity.getVelocity().multiply(0.5));
            return;
        }
        this.entity.setVelocity(this.entity.getVelocity().add(toTarget.multiply(this.speed * 0.05 * this.speedTuning / distance)));
        Vec3d velocity = this.entity.getVelocity();
        double lookX;
        double lookZ;
        if (this.entity.getTarget() != null) {
            lookX = this.entity.getTarget().getX() - this.entity.getX();
            lookZ = this.entity.getTarget().getZ() - this.entity.getZ();
        } else {
            lookX = velocity.x;
            lookZ = velocity.z;
        }
        this.entity.setYaw(-((float) MathHelper.atan2(lookX, lookZ)) * (180.0F / (float) Math.PI));
        this.entity.bodyYaw = this.entity.getYaw();
    }
}
