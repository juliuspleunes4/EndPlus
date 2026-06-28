package com.endplus.entity.dragon;

import java.util.Set;
import java.util.UUID;

public interface EnderDragonPhaseData {
    DragonPhase endplus_getCurrentPhase();
    boolean endplus_isShieldActive();
    void endplus_deactivateShield();
    long endplus_getFightStartTick();
    Set<UUID> endplus_getParticipants();
}
