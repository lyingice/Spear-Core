package net.minecraft.spearcore.mixin;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.spearcore.client.animation.SpearAnimations;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.util.SpearCooldownAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySpearMixin extends Entity implements SpearCooldownAccessor {

    @Unique
    @Nullable
    private Object2LongMap<Entity> RecentStabbedEntities;

    public LivingEntitySpearMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "startUsingItem", at = @At("TAIL"))
    private void onStartUsingItem(InteractionHand hand, CallbackInfo ci) {
        if (!this.level().isClientSide) {
            LivingEntity self = (LivingEntity) (Object) this;
            ItemStack stack = self.getItemInHand(hand);
            if (stack.getItem() instanceof SpearItem) {
                this.RecentStabbedEntities = new Object2LongOpenHashMap<>();
            }
        }
    }

    @Override
    @Unique
    public boolean WasRecentlyStabbed(Entity target, int cooldownTicks) {
        if (this.RecentStabbedEntities == null) return false;
        return this.level().getGameTime() - this.RecentStabbedEntities.getLong(target) < (long) cooldownTicks;
    }

    @Override
    @Unique
    public void RememberStabbedEntity(Entity target) {
        if (this.RecentStabbedEntities != null) {
            this.RecentStabbedEntities.put(target, this.level().getGameTime());
        }
    }
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void onHandleEntityEvent(byte eventId, CallbackInfo ci) {
        if (eventId == 2) {
            // 命中反馈，触发动画
            if (this.level().isClientSide) {
                SpearAnimations.triggerHitFeedback();
            }
        }
    }
    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    private void onGetCurrentSwingDuration(CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack stack = self.getMainHandItem();
        if (stack.getItem() instanceof SpearItem spear) {
            int n = (int) (spear.getSwingTimes() * 20.0F);
            if (net.minecraft.world.effect.MobEffectUtil.hasDigSpeed(self)) {
                cir.setReturnValue(n - (1 + net.minecraft.world.effect.MobEffectUtil.getDigSpeedAmplification(self)));
            } else if (self.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN)) {
                cir.setReturnValue(n + (1 + self.getEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2);
            } else {
                cir.setReturnValue(n);
            }
            cir.cancel();
        }
    }
}