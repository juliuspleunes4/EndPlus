package com.endplus.mixin.entity.dragon;

import com.endplus.EndPlus;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonEntity.class)
public abstract class DragonHealthMixin extends MobEntity {

    protected DragonHealthMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void endplus_initHealth(EntityType<? extends EnderDragonEntity> type, World world, CallbackInfo ci) {
        EntityAttributeInstance healthAttr = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(EndPlus.CONFIG.dragon.maxHealth);
            this.setHealth(EndPlus.CONFIG.dragon.maxHealth);
        }
    }
}
