package net.minecraft.spearcore.mixin;

import net.minecraft.spearcore.client.animation.SpearAnimations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySpearClientMixin {
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void spearcore$handleSpearHitFeedback(byte eventId, CallbackInfo ci) {
        if (eventId == 2) {
            SpearAnimations.triggerHitFeedback();
        }
    }
}