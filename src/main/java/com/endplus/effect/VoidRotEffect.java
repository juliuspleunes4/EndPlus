package com.endplus.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;

public class VoidRotEffect extends StatusEffect {

    public VoidRotEffect() {
        super(StatusEffectCategory.HARMFUL, 0x1a0030);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int interval = 25 >> amplifier;
        return interval == 0 || duration % interval == 0;
    }

    @Override
    public void applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        entity.damage(world, entity.getDamageSources().magic(), 1.0f + amplifier);
    }
}
