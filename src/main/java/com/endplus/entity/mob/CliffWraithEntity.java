package com.endplus.entity.mob;

import com.endplus.entity.ai.FlyingCircleGoal;
import com.endplus.entity.ai.FlyingMoveControl;
import com.endplus.entity.ai.FlyingSwoopGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Fast, hit-and-run flyer of the Skyreach Peaks (PLANNING 6.4). Swoops in from above, hurls its
 * target about five blocks backwards and has a 10% chance to knock the held item out of their hand,
 * landing it three to eight blocks away. Hunts in pairs and takes no fall damage.
 */
public class CliffWraithEntity extends HostileEntity {

    private static final float DISARM_CHANCE = 0.10f;
    private static final int MIN_ITEM_THROW = 3;
    private static final int MAX_ITEM_THROW = 8;
    private static final double PAIR_RANGE = 24.0;

    public CliffWraithEntity(EntityType<? extends CliffWraithEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlyingMoveControl(this, 1.8F);
        this.setNoGravity(true);
        this.experiencePoints = 6;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                // takeKnockback(strength = this * 0.5): ~2.0 sends a grounded player about 5 blocks.
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 4.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new FlyingSwoopGoal(this, 40));
        this.goalSelector.add(3, new FlyingCircleGoal(this));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (!this.getWorld().isClient() && this.age % 20 == 0) {
            this.shareTargetWithPartner();
        }
    }

    /** Pair hunting: a wraith with a target recruits the nearest idle wraith. */
    private void shareTargetWithPartner() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }
        List<CliffWraithEntity> idle = this.getWorld().getEntitiesByClass(CliffWraithEntity.class,
                this.getBoundingBox().expand(PAIR_RANGE),
                other -> other != this && other.isAlive() && other.getTarget() == null);
        CliffWraithEntity partner = null;
        for (CliffWraithEntity other : idle) {
            if (partner == null || this.squaredDistanceTo(other) < this.squaredDistanceTo(partner)) {
                partner = other;
            }
        }
        if (partner != null) {
            partner.setTarget(target);
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof PlayerEntity player && !player.isCreative()
                && this.random.nextFloat() < DISARM_CHANCE) {
            this.disarm(player);
        }
        return hit;
    }

    /** Knocks the held item out of the player's hand and lands it a few blocks away on solid ground. */
    private void disarm(PlayerEntity player) {
        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            return;
        }
        ItemStack thrown = held.copy();
        player.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        double x = player.getX();
        double y = player.getY() + 0.5;
        double z = player.getZ();
        for (int attempt = 0; attempt < 8; attempt++) {
            double distance = MIN_ITEM_THROW + this.random.nextDouble() * (MAX_ITEM_THROW - MIN_ITEM_THROW);
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double tx = player.getX() + Math.cos(angle) * distance;
            double tz = player.getZ() + Math.sin(angle) * distance;
            BlockPos column = BlockPos.ofFloored(tx, player.getY() + 2.0, tz);
            for (int dy = 0; dy < 8; dy++) {
                BlockPos below = column.down(dy);
                if (this.getWorld().getBlockState(below).isSolidBlock(this.getWorld(), below)
                        && this.getWorld().getBlockState(below.up()).isAir()) {
                    x = tx;
                    y = below.getY() + 1.0;
                    z = tz;
                    attempt = 8;
                    break;
                }
            }
        }
        ItemEntity drop = new ItemEntity(this.getWorld(), x, y, z, thrown);
        drop.setPickupDelay(30);
        this.getWorld().spawnEntity(drop);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PHANTOM_DEATH;
    }
}
