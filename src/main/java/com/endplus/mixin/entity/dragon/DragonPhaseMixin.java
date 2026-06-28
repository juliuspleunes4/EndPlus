package com.endplus.mixin.entity.dragon;

import com.endplus.EndPlus;
import com.endplus.entity.dragon.DragonPhase;
import com.endplus.entity.dragon.EnderDragonPhaseData;
import com.endplus.entity.projectile.VoidBeamEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(EnderDragonEntity.class)
public abstract class DragonPhaseMixin extends MobEntity implements EnderDragonPhaseData {

    @Unique private DragonPhase endplus_phase = DragonPhase.PHASE_1;
    @Unique private boolean endplus_shieldActive = false;
    @Unique private int endplus_shieldReactivateTicks = 0;
    @Unique private final UUID[] endplus_crystalIds = new UUID[2];
    @Unique private int endplus_crystalCheckCooldown = 0;
    @Unique private int endplus_beamCooldown = 0;
    @Unique private long endplus_fightStartTick = -1L;
    @Unique private final Set<UUID> endplus_participants = new HashSet<>();

    protected DragonPhaseMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void endplus_onInit(EntityType<? extends EnderDragonEntity> type, World world, CallbackInfo ci) {
        endplus_fightStartTick = world.getTime();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void endplus_onTick(CallbackInfo ci) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        float healthPct = (this.getHealth() / this.getMaxHealth()) * 100f;
        int[] thresholds = EndPlus.CONFIG.dragon.phaseThresholds;
        DragonPhase newPhase = DragonPhase.fromHealthPercent(healthPct, thresholds);

        if (newPhase != endplus_phase) {
            endplus_onPhaseTransition(serverWorld, newPhase);
            endplus_phase = newPhase;
        }

        if (endplus_shieldActive) {
            if (--endplus_crystalCheckCooldown <= 0) {
                endplus_crystalCheckCooldown = 20;
                endplus_checkVoidCrystals(serverWorld);
            }
        } else if (endplus_shieldReactivateTicks > 0) {
            if (--endplus_shieldReactivateTicks == 0 && (endplus_phase == DragonPhase.PHASE_3 || endplus_phase == DragonPhase.PHASE_4)) {
                endplus_activateShield(serverWorld);
            }
        }

        if (endplus_phase == DragonPhase.PHASE_4) {
            if (endplus_beamCooldown > 0) {
                endplus_beamCooldown--;
            } else {
                endplus_fireVoidBeam(serverWorld);
                endplus_beamCooldown = 200 + serverWorld.getRandom().nextInt(201);
            }
        }
    }

    @Inject(method = "getXpToDrop", at = @At("HEAD"), cancellable = true)
    private void endplus_overrideXp(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(EndPlus.CONFIG.dragon.xpReward);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void endplus_trackParticipant(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            endplus_participants.add(player.getUuid());
        }
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float endplus_applyShieldReduction(float amount) {
        if (endplus_shieldActive) {
            return amount * (1.0f - (float) EndPlus.CONFIG.dragon.voidShieldReduction);
        }
        return amount;
    }

    @Unique
    private void endplus_onPhaseTransition(ServerWorld world, DragonPhase newPhase) {
        String message = switch (newPhase) {
            case PHASE_2 -> "§5The dragon roars — it calls for reinforcements!";
            case PHASE_3 -> "§5A Void Shield surrounds the dragon!";
            case PHASE_4 -> "§4The dragon enters a rage!";
            default -> null;
        };
        if (message != null) {
            for (ServerPlayerEntity player : world.getPlayers()) {
                player.sendMessage(Text.literal(message), true);
            }
        }
        if (newPhase == DragonPhase.PHASE_3) {
            endplus_activateShield(world);
        }
    }

    @Unique
    private void endplus_activateShield(ServerWorld world) {
        endplus_shieldActive = true;

        EndCrystalEntity crystal1 = new EndCrystalEntity(world, 20.0, 70.0, 0.0);
        crystal1.setBeamTarget(null);
        world.spawnEntity(crystal1);
        endplus_crystalIds[0] = crystal1.getUuid();

        EndCrystalEntity crystal2 = new EndCrystalEntity(world, -20.0, 70.0, 0.0);
        crystal2.setBeamTarget(null);
        world.spawnEntity(crystal2);
        endplus_crystalIds[1] = crystal2.getUuid();

        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(Text.literal("§5Two Void Crystals appear — destroy them to break the shield!"), true);
        }
    }

    @Unique
    private void endplus_checkVoidCrystals(ServerWorld world) {
        boolean c1Dead = endplus_crystalIds[0] == null || world.getEntity(endplus_crystalIds[0]) == null;
        boolean c2Dead = endplus_crystalIds[1] == null || world.getEntity(endplus_crystalIds[1]) == null;

        if (c1Dead && c2Dead) {
            endplus_shieldActive = false;
            endplus_crystalIds[0] = null;
            endplus_crystalIds[1] = null;
            endplus_shieldReactivateTicks = 90 * 20;
            for (ServerPlayerEntity player : world.getPlayers()) {
                player.sendMessage(Text.literal("§aThe Void Shield has been broken!"), true);
            }
        }
    }

    @Unique
    private void endplus_fireVoidBeam(ServerWorld world) {
        PlayerEntity target = world.getClosestPlayer(this, 100.0);
        if (target == null) return;

        Vec3d origin = new Vec3d(this.getX(), this.getBodyY(0.5), this.getZ());
        Vec3d targetCenter = target.getPos().add(0, target.getHeight() / 2.0, 0);
        Vec3d velocity = targetCenter.subtract(origin).normalize().multiply(0.6);

        VoidBeamEntity beam = new VoidBeamEntity(world, (EnderDragonEntity)(Object)this, velocity.x, velocity.y, velocity.z);
        beam.setPosition(origin.x, origin.y, origin.z);
        world.spawnEntity(beam);
    }

    @Override
    @Unique
    public DragonPhase endplus_getCurrentPhase() {
        return endplus_phase;
    }

    @Override
    @Unique
    public boolean endplus_isShieldActive() {
        return endplus_shieldActive;
    }

    @Override
    @Unique
    public void endplus_deactivateShield() {
        endplus_shieldActive = false;
    }

    @Override
    @Unique
    public long endplus_getFightStartTick() {
        return endplus_fightStartTick;
    }

    @Override
    @Unique
    public Set<UUID> endplus_getParticipants() {
        return endplus_participants;
    }
}
