package com.endplus.entity.minion;

import com.endplus.registry.ModEffects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class VoidWitchEntity extends HostileEntity {

    private int healTimer;
    private int throwTimer;
    private int blinkCooldown;

    public VoidWitchEntity(EntityType<? extends VoidWitchEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 6;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new FleeEntityGoal<>(this, PlayerEntity.class, 6.0f, 1.0, 1.3));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
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
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        if (++this.healTimer >= 60) {
            this.healTimer = 0;
            this.healNearbyMinions(serverWorld);
        }

        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }

        double distanceSq = this.squaredDistanceTo(target);
        if (distanceSq < 25.0 && this.blinkCooldown == 0) {
            this.blinkAwayFrom(target);
            return;
        }

        if (distanceSq < 256.0 && this.getVisibilityCache().canSee(target) && ++this.throwTimer >= 70) {
            this.throwTimer = 0;
            this.throwPotion(serverWorld, target);
        }
    }

    private void healNearbyMinions(ServerWorld world) {
        List<HostileEntity> nearby = world.getEntitiesByClass(
                HostileEntity.class, this.getBoundingBox().expand(10.0),
                e -> e != this && (e instanceof VoidImpEntity || e instanceof EnderPhantomEntity
                        || e instanceof EndriteGolemEntity || e instanceof ShadowDrakeEntity)
        );
        for (HostileEntity minion : nearby) {
            if (minion.getHealth() < minion.getMaxHealth()) {
                minion.heal(4.0f);
            }
        }
    }

    private void blinkAwayFrom(LivingEntity threat) {
        double dx = this.getX() - threat.getX();
        double dz = this.getZ() - threat.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0e-4) {
            dx = this.getRandom().nextDouble() - 0.5;
            dz = this.getRandom().nextDouble() - 0.5;
            length = Math.sqrt(dx * dx + dz * dz);
        }
        double step = 8.0 + this.getRandom().nextDouble() * 4.0;
        double x = this.getX() + dx / length * step;
        double z = this.getZ() + dz / length * step;
        if (this.teleport(x, this.getY(), z, true)) {
            this.blinkCooldown = 60;
        }
    }

    private void throwPotion(ServerWorld world, LivingEntity target) {
        ItemStack potionStack = new ItemStack(Items.SPLASH_POTION);
        PotionContentsComponent contents = new PotionContentsComponent(
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0),
                        new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 0),
                        new StatusEffectInstance(ModEffects.VOID_ROT, 100, 0)
                )
        );
        potionStack.set(DataComponentTypes.POTION_CONTENTS, contents);

        PotionEntity potion = new PotionEntity(world, this);
        potion.setItem(potionStack);
        Vec3d toTarget = target.getPos().add(0, target.getHeight() / 2.0, 0)
                .subtract(this.getX(), this.getEyeY(), this.getZ());
        potion.setVelocity(toTarget.x, toTarget.y + toTarget.horizontalLength() * 0.2, toTarget.z, 0.75f, 8.0f);
        world.spawnEntity(potion);
    }
}
