package com.endplus.entity.minion;

import com.endplus.registry.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SplashPotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionContentsComponent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class VoidWitchEntity extends HostileEntity {

    private int endplus_healTimer = 0;
    private int endplus_throwTimer = 0;

    public VoidWitchEntity(EntityType<? extends VoidWitchEntity> type, World world) {
        super(type, world);
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
        this.goalSelector.add(1, new FleeEntityGoal<>(this, PlayerEntity.class, 5.0f, 1.0, 1.2));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            if (++endplus_healTimer >= 60) {
                endplus_healTimer = 0;
                endplus_healNearbyMinions(serverWorld);
            }
            LivingEntity target = this.getTarget();
            if (target != null && ++endplus_throwTimer >= 40) {
                endplus_throwTimer = 0;
                endplus_throwPotion(serverWorld, target);
            }
        }
    }

    private void endplus_healNearbyMinions(ServerWorld world) {
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

    private void endplus_throwPotion(ServerWorld world, LivingEntity target) {
        ItemStack potionStack = new ItemStack(Items.SPLASH_POTION);
        PotionContentsComponent contents = new PotionContentsComponent(
                java.util.Optional.empty(),
                java.util.Optional.empty(),
                List.of(
                        new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0),
                        new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 0),
                        new StatusEffectInstance(ModEffects.VOID_ROT, 100, 0)
                )
        );
        potionStack.set(net.minecraft.component.DataComponentTypes.POTION_CONTENTS, contents);

        SplashPotionEntity potion = new SplashPotionEntity(world, this, potionStack);
        Vec3d toTarget = target.getPos().add(0, target.getHeight() / 2.0, 0)
                .subtract(this.getX(), this.getEyeY(), this.getZ());
        potion.setVelocity(toTarget.x, toTarget.y + toTarget.horizontalLength() * 0.2, toTarget.z,
                0.75f, 8.0f);
        world.spawnEntity(potion);
    }
}
