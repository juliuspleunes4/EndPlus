package com.endplus.entity.mob;

import com.endplus.advancement.EndPlusAdvancements;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SpiderNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

/**
 * Skittering ceiling dweller (PLANNING 6.6). Harmless until a player strays within two blocks, then it
 * latches on and drains 1 HP per second for five seconds. Sprinting for two seconds shakes it off.
 * It rests hanging from ceilings and drops onto players who walk beneath it.
 */
public class EnderParasiteEntity extends PathAwareEntity {

    private static final double LATCH_RANGE = 2.0;
    private static final int DRAIN_INTERVAL = 20;
    private static final int LATCH_DURATION = 100;
    private static final int SHAKE_OFF_TICKS = 40;
    private static final int REATTACH_COOLDOWN = 200;
    private static final double DROP_HORIZONTAL_RANGE = 3.0;
    private static final int DROP_VERTICAL_RANGE = 8;
    private static final float DRAIN_DAMAGE = 1.0f;
    private static final int SIMULTANEOUS_FOR_ADVANCEMENT = 3;

    private static final TrackedData<Boolean> HANGING =
            DataTracker.registerData(EnderParasiteEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Byte> CLIMBING_FLAG =
            DataTracker.registerData(EnderParasiteEntity.class, TrackedDataHandlerRegistry.BYTE);

    private int latchTicks;
    private int sprintTicks;
    private int cooldown;

    public EnderParasiteEntity(EntityType<? extends EnderParasiteEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 2;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(HANGING, false);
        builder.add(CLIMBING_FLAG, (byte) 0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SpiderNavigation(this, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new AscendToCeilingGoal(this));
        this.goalSelector.add(4, new FreeRoamGoal(this));
        this.goalSelector.add(6, new LookAroundGoal(this));
    }

    public boolean isHanging() {
        return this.dataTracker.get(HANGING);
    }

    public boolean isLatched() {
        return this.getVehicle() instanceof PlayerEntity;
    }

    @Override
    public boolean isClimbing() {
        return (this.dataTracker.get(CLIMBING_FLAG) & 1) != 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            return;
        }
        this.setClimbingFlag(this.horizontalCollision && !this.isHanging());
        if (this.cooldown > 0) {
            this.cooldown--;
        }
        if (this.isLatched()) {
            this.tickLatched();
            return;
        }
        if (this.isHanging()) {
            this.tickHanging();
        } else {
            this.tryHang();
        }
        if (this.cooldown == 0) {
            PlayerEntity victim = this.getWorld().getClosestPlayer(this, LATCH_RANGE);
            if (victim != null && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(victim) && this.canSee(victim)) {
                this.latchOnto(victim);
            }
        }
    }

    private void setClimbingFlag(boolean climbing) {
        byte flags = this.dataTracker.get(CLIMBING_FLAG);
        byte updated = climbing ? (byte) (flags | 1) : (byte) (flags & ~1);
        if (updated != flags) {
            this.dataTracker.set(CLIMBING_FLAG, updated);
        }
    }

    // -- latching -------------------------------------------------------------------------------

    private void latchOnto(PlayerEntity victim) {
        this.setHanging(false);
        this.startRiding(victim, true);
        this.latchTicks = 0;
        this.sprintTicks = 0;
        this.getNavigation().stop();
        this.playSound(SoundEvents.ENTITY_ENDERMITE_AMBIENT, 1.0f, 0.6f);
        if (victim instanceof ServerPlayerEntity player) {
            long latched = victim.getPassengerList().stream().filter(EnderParasiteEntity.class::isInstance).count();
            if (latched >= SIMULTANEOUS_FOR_ADVANCEMENT) {
                EndPlusAdvancements.grant(player, "mobs/parasite_survivor");
            }
        }
    }

    private void tickLatched() {
        PlayerEntity host = (PlayerEntity) this.getVehicle();
        this.latchTicks++;
        this.sprintTicks = host.isSprinting() ? this.sprintTicks + 1 : 0;
        if (this.sprintTicks >= SHAKE_OFF_TICKS) {
            this.detach(true);
            return;
        }
        if (this.latchTicks % DRAIN_INTERVAL == 0) {
            host.damage(this.getWorld().getDamageSources().magic(), DRAIN_DAMAGE);
            if (!host.isAlive()) {
                this.detach(false);
                return;
            }
        }
        if (this.latchTicks >= LATCH_DURATION) {
            this.detach(false);
        }
    }

    private void detach(boolean flung) {
        this.stopRiding();
        this.cooldown = REATTACH_COOLDOWN;
        if (flung) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            this.setVelocity(Math.cos(angle) * 0.5, 0.4, Math.sin(angle) * 0.5);
            this.velocityModified = true;
            this.playSound(SoundEvents.ENTITY_ENDERMITE_HURT, 1.0f, 1.2f);
        }
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        this.latchTicks = 0;
        this.sprintTicks = 0;
    }

    // -- ceiling dwelling -----------------------------------------------------------------------

    private void setHanging(boolean hanging) {
        if (hanging != this.isHanging()) {
            this.dataTracker.set(HANGING, hanging);
            this.setNoGravity(hanging);
            if (hanging) {
                this.setVelocity(0, 0, 0);
                this.getNavigation().stop();
            }
        }
    }

    private boolean hasCeilingAbove() {
        BlockPos above = BlockPos.ofFloored(this.getX(), this.getBoundingBox().maxY + 0.15, this.getZ());
        BlockState state = this.getWorld().getBlockState(above);
        return state.isSolidBlock(this.getWorld(), above);
    }

    private void tryHang() {
        if (this.cooldown == 0 && this.hasCeilingAbove() && this.random.nextInt(100) == 0) {
            this.setHanging(true);
        }
    }

    private void tickHanging() {
        this.setVelocity(0, 0, 0);
        if (!this.hasCeilingAbove()) {
            this.setHanging(false);
            return;
        }
        List<PlayerEntity> below = this.getWorld().getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(DROP_HORIZONTAL_RANGE, 0, DROP_HORIZONTAL_RANGE)
                        .stretch(0, -DROP_VERTICAL_RANGE, 0),
                player -> EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(player) && this.canSee(player));
        if (!below.isEmpty()) {
            this.setHanging(false);
            this.playSound(SoundEvents.ENTITY_ENDERMITE_AMBIENT, 1.0f, 0.5f);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean result = super.damage(source, amount);
        if (result && !this.getWorld().isClient()) {
            this.setHanging(false);
        }
        return result;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.isHanging() && !this.isLatched();
    }

    // -- goals ----------------------------------------------------------------------------------

    /** Ordinary wandering, but only while on the ground or walls (not hanging, not latched). */
    private static class FreeRoamGoal extends WanderAroundGoal {
        private final EnderParasiteEntity parasite;

        FreeRoamGoal(EnderParasiteEntity parasite) {
            super(parasite, 0.8, 80);
            this.parasite = parasite;
        }

        @Override
        public boolean canStart() {
            return !this.parasite.isHanging() && !this.parasite.isLatched() && super.canStart();
        }
    }

    /**
     * Climbs a wall straight up to the ceiling to hang there. Only starts where an unbroken wall runs all
     * the way from the parasite to the ceiling, so it really does crawl up the stone.
     */
    private static class AscendToCeilingGoal extends Goal {
        private static final int MAX_HEIGHT = 8;
        private final EnderParasiteEntity parasite;
        private Direction wall;

        AscendToCeilingGoal(EnderParasiteEntity parasite) {
            this.parasite = parasite;
            this.setControls(EnumSet.of(Control.MOVE, Control.JUMP));
        }

        @Override
        public boolean canStart() {
            if (this.parasite.isHanging() || this.parasite.isLatched() || this.parasite.cooldown > 0
                    || this.parasite.getRandom().nextInt(300) != 0) {
                return false;
            }
            this.wall = this.findClimbableWall();
            return this.wall != null;
        }

        private Direction findClimbableWall() {
            World world = this.parasite.getWorld();
            BlockPos base = this.parasite.getBlockPos();
            for (Direction side : Direction.Type.HORIZONTAL) {
                for (int height = 2; height <= MAX_HEIGHT; height++) {
                    BlockPos ceiling = base.up(height);
                    if (world.getBlockState(ceiling).isSolidBlock(world, ceiling)) {
                        boolean wallComplete = true;
                        for (int step = 0; step < height && wallComplete; step++) {
                            BlockPos column = base.up(step);
                            BlockPos wallBlock = column.offset(side);
                            wallComplete = world.getBlockState(column).isAir()
                                    && world.getBlockState(wallBlock).isSolidBlock(world, wallBlock);
                        }
                        if (wallComplete) {
                            return side;
                        }
                    } else if (!world.getBlockState(ceiling).isAir()) {
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        public boolean shouldContinue() {
            return !this.parasite.isHanging() && !this.parasite.isLatched()
                    && !this.parasite.hasCeilingAbove();
        }

        @Override
        public void start() {
            this.parasite.getNavigation().stop();
            this.parasite.setNoGravity(true);
        }

        @Override
        public void tick() {
            if (this.wall != null) {
                // Press against the wall while creeping upwards.
                this.parasite.setVelocity(this.wall.getOffsetX() * 0.05, 0.15, this.wall.getOffsetZ() * 0.05);
            }
        }

        @Override
        public void stop() {
            this.parasite.setNoGravity(this.parasite.isHanging());
            if (this.parasite.hasCeilingAbove() && !this.parasite.isLatched()) {
                this.parasite.setHanging(true);
            }
        }
    }

    // -- sounds ---------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDERMITE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDERMITE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMITE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_ENDERMITE_STEP, 0.15f, 1.0f);
    }

    @Override
    public boolean isPushable() {
        return !this.isLatched() && super.isPushable();
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return entity instanceof PlayerEntity;
    }
}
