package com.endplus.mixin.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AreaEffectCloudEntity.class)
public abstract class AreaEffectCloudBreathMixin {

    @Unique private boolean endplus_breathScaled = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void endplus_scaleBreath(CallbackInfo ci) {
        if (endplus_breathScaled) return;
        AreaEffectCloudEntity self = (AreaEffectCloudEntity)(Object)this;
        if (self.getWorld().isClient()) return;
        Entity owner = self.getOwner();
        if (owner instanceof EnderDragonEntity) {
            self.setRadius(self.getRadius() * 1.3f);
            self.setDuration((int)(self.getDuration() * 1.5f));
            endplus_breathScaled = true;
        }
    }
}
