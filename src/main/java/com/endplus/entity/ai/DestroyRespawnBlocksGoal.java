package com.endplus.entity.ai;

import net.minecraft.block.BedBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Sends a flyer after player-placed respawn points (beds, respawn anchors) inside the arena and
 * removes them on contact. Mirrors the Shadow Drake behaviour in PLANNING.md §3.5 — denying a
 * safe respawn during the rage phase.
 */
public class DestroyRespawnBlocksGoal extends Goal {

    private final MobEntity mob;
    private final double searchRadius;
    private BlockPos targetBlock;
    private int rescanCooldown;

    public DestroyRespawnBlocksGoal(MobEntity mob, double searchRadius) {
        this.mob = mob;
        this.searchRadius = searchRadius;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (this.rescanCooldown > 0) {
            this.rescanCooldown--;
            return false;
        }
        this.rescanCooldown = 40;
        this.targetBlock = this.findNearbyRespawnBlock();
        return this.targetBlock != null;
    }

    @Override
    public boolean shouldContinue() {
        return this.targetBlock != null && this.isRespawnBlock(this.mob.getWorld(), this.targetBlock);
    }

    @Override
    public void start() {
        if (this.targetBlock != null) {
            this.mob.getMoveControl().moveTo(this.targetBlock.getX() + 0.5, this.targetBlock.getY() + 0.5, this.targetBlock.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public void stop() {
        this.targetBlock = null;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.targetBlock == null) {
            return;
        }
        this.mob.getMoveControl().moveTo(this.targetBlock.getX() + 0.5, this.targetBlock.getY() + 0.5, this.targetBlock.getZ() + 0.5, 1.0);
        if (this.mob.squaredDistanceTo(this.targetBlock.getX() + 0.5, this.targetBlock.getY() + 0.5, this.targetBlock.getZ() + 0.5) < 4.0) {
            World world = this.mob.getWorld();
            if (this.isRespawnBlock(world, this.targetBlock)) {
                world.breakBlock(this.targetBlock, false, this.mob);
            }
            this.targetBlock = null;
        }
    }

    private BlockPos findNearbyRespawnBlock() {
        BlockPos origin = this.mob.getBlockPos();
        int range = (int) this.searchRadius;
        BlockPos.Mutable cursor = new BlockPos.Mutable();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (this.isRespawnBlock(this.mob.getWorld(), cursor)) {
                        double distance = this.mob.squaredDistanceTo(cursor.getX() + 0.5, cursor.getY() + 0.5, cursor.getZ() + 0.5);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = cursor.toImmutable();
                        }
                    }
                }
            }
        }
        return best;
    }

    private boolean isRespawnBlock(World world, BlockPos pos) {
        net.minecraft.block.Block block = world.getBlockState(pos).getBlock();
        return block instanceof BedBlock || block instanceof RespawnAnchorBlock;
    }
}
