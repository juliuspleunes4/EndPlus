package com.endplus.advancement;

import com.endplus.EndPlus;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Grants End+ advancements whose criteria are {@code minecraft:impossible} placeholders completed from code. */
public final class EndPlusAdvancements {

    private EndPlusAdvancements() {
    }

    public static void grant(ServerPlayerEntity player, String path) {
        AdvancementEntry advancement = player.server.getAdvancementLoader().get(Identifier.of(EndPlus.MOD_ID, path));
        if (advancement == null) {
            return;
        }
        PlayerAdvancementTracker tracker = player.getAdvancementTracker();
        for (String criterion : tracker.getProgress(advancement).getUnobtainedCriteria()) {
            tracker.grantCriterion(advancement, criterion);
        }
    }
}
