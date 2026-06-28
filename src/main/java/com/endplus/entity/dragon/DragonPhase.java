package com.endplus.entity.dragon;

public enum DragonPhase {
    PHASE_1,
    PHASE_2,
    PHASE_3,
    PHASE_4;

    public static DragonPhase fromHealthPercent(float percent, int[] thresholds) {
        if (percent > thresholds[0]) return PHASE_1;
        if (percent > thresholds[1]) return PHASE_2;
        if (percent > thresholds[2]) return PHASE_3;
        return PHASE_4;
    }
}
