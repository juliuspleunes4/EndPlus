package com.endplus.mixin.entity.dragon;

import com.endplus.EndPlus;
import com.endplus.entity.dragon.EnderDragonPhaseData;
import com.endplus.registry.ModItems;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EnderDragonFight.class)
public class DragonFightMixin {

    @Shadow private ServerWorld world;

    @Inject(method = "dragonKilled", at = @At("TAIL"))
    private void endplus_onDragonKilled(EnderDragonEntity dragon, CallbackInfo ci) {
        double x = dragon.getX();
        double y = dragon.getY() + 1.0;
        double z = dragon.getZ();

        world.spawnEntity(new ItemEntity(world, x, y, z, new ItemStack(ModItems.DRAGON_HEART)));

        int scaleCount = 3 + world.getRandom().nextInt(4);
        world.spawnEntity(new ItemEntity(world, x, y, z, new ItemStack(ModItems.VOID_SCALE, scaleCount)));

        if (dragon instanceof EnderDragonPhaseData phaseData) {
            long fightDurationTicks = world.getTime() - phaseData.endplus_getFightStartTick();
            if (fightDurationTicks < 10L * 60 * 20) {
                for (UUID participantId : phaseData.endplus_getParticipants()) {
                    ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(participantId);
                    if (player != null) {
                        endplus_grantAdvancement(player, "dragon/swift_slayer");
                    }
                }
            }
        }
    }

    @Unique
    private void endplus_grantAdvancement(ServerPlayerEntity player, String path) {
        AdvancementEntry advancement = world.getServer().getAdvancementLoader()
                .get(Identifier.of(EndPlus.MOD_ID, path));
        if (advancement == null) return;
        ServerAdvancementTracker tracker = player.getAdvancementTracker();
        AdvancementProgress progress = tracker.getProgress(advancement);
        for (String criterion : progress.getUnobtainedRequirements()) {
            tracker.grantCriterion(advancement, criterion);
        }
    }
}
