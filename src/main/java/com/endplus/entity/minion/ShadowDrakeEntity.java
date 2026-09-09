package com.endplus.entity.minion;

import com.endplus.entity.ai.CrystalPerchHealGoal;
import com.endplus.entity.ai.DestroyRespawnBlocksGoal;
import com.endplus.entity.ai.FlyingCircleGoal;
import com.endplus.entity.ai.FlyingMoveControl;
import com.endplus.entity.ai.FlyingSwoopGoal;
import com.endplus.registry.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ShadowDrakeEntity extends HostileEntity {

    public ShadowDrakeEntity(EntityType<? extends ShadowDrakeEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlyingMoveControl(this, 1.15F);
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 80.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new FlyingSwoopGoal(this, 70));
        this.goalSelector.add(2, new CrystalPerchHealGoal(this));
        this.goalSelector.add(3, new DestroyRespawnBlocksGoal(this, 10.0));
        this.goalSelector.add(4, new FlyingCircleGoal(this));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        super.move(movementType, movement);
        this.checkBlockCollision();
    }

    @Override
    public void tick() {
        this.noClip = true;
        super.tick();
        this.noClip = false;
        this.setNoGravity(true);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(ModEffects.VOID_ROT, 60, 1));
        }
        return hit;
    }
}
