package net.minecraft.spearcore.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.spearcore.util.KnownMovementAccessor;
import net.minecraft.spearcore.util.SpearCollision;
import net.minecraft.spearcore.util.SpearCondition;
import net.minecraft.spearcore.util.SpearCooldownAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.Optional;


public abstract class SpearItem extends Item {

    public SpearItem(Properties properties) {
        super(properties);
    }

    // ========== 基础属性 ==========
    public abstract float getAttackDuration();
    public abstract float getDamageMultiplier();
    public abstract SoundEvent getUseSound();
    public abstract SoundEvent getHitSound();
    public abstract SoundEvent getAttackSound();

    // ========== 蓄力阶段相关 ==========
    public abstract int getDelayTicks();
    public abstract int getDismountEndTick();
    public abstract int getKnockbackEndTick();
    public abstract int getDamageEndTick();
    public abstract Optional<SpearCondition> getDismountConditions();
    public abstract Optional<SpearCondition> getKnockbackConditions();
    public abstract Optional<SpearCondition> getDamageConditions();
    public abstract float getForwardMovement();
    public abstract float getMinRange();
    public abstract float getMaxRange();
    public abstract float getHitboxMargin();
    public abstract float getHitboxMargin2();
    public abstract int getContactCooldownTicks();
    public abstract float getSwingTimes();

    protected int getSpearEnchantmentValue() { return 0; }
    protected boolean canRepair(ItemStack stack, ItemStack repairCandidate) { return false; }

    // ========== 静态工具 ==========

    public static Vec3 getMotion(Entity entity) {
        // 骑乘时用坐骑的 MOVEMENT_SPEED 属性
        if (entity.isPassenger() && entity.getVehicle() != null) {
            Entity vehicle = entity.getVehicle();
            Vec3 motion = vehicle.position().subtract(vehicle.xo, vehicle.yo, vehicle.zo);
            if (motion.lengthSqr() > 0.0001) {
                Vec3 vec3 = motion.scale(20.0);
                if (vehicle.onGround()) {
                    return vec3.with(net.minecraft.core.Direction.Axis.Y, 0.0);
                }
                return vec3;
            }
        }

        // 玩家用已知移动
        if (entity instanceof ServerPlayer serverPlayer && entity.isAlive()) {
            Vec3 known = ((KnownMovementAccessor) serverPlayer).GetKnownMovement();
            if (known.lengthSqr() > 0.001) {
                return known.scale(20.0);
            }
        }

        // fallback
        Vec3 motion = entity.position().subtract(entity.xo, entity.yo, entity.zo);
        if (motion.lengthSqr() < 0.0001) {
            motion = entity.getDeltaMovement();
        }

        Vec3 vec3 = motion.scale(20.0);
        if (entity.onGround()) {
            return vec3.with(net.minecraft.core.Direction.Axis.Y, 0.0);
        }
        return vec3;
    }

    // ========== 物品行为 ==========

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                getUseSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingTicks) {
    }

    // ========== 蓄力 tick ==========

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingTicks) {

        if (level.isClientSide) return;
        if (!(user instanceof Player player)) return;

        if (player.getCooldowns().isOnCooldown(this)) {
            player.stopUsingItem();
            return;
        }

        int usedTicks = stack.getUseDuration(player) - remainingTicks;

        if (usedTicks < getDelayTicks()) return;

        int effectiveTicks = usedTicks - getDelayTicks();
        Vec3 look = player.getLookAngle();
        double attackerSpeed = look.dot(SpearItem.getMotion(player));
        double needSpeed = 1.0F; // 玩家固定 1.0

        // 脱离阶段
        if (usedTicks >= getDamageEndTick()) {
            player.stopUsingItem();
            return;
        }

        boolean hitSomething = false;

        List<EntityHitResult> hits = SpearCollision.getHitEntitiesAlong(
                player, this, getHitboxMargin(),
                entity -> entity instanceof LivingEntity
                        && entity.isAlive()
                        && entity != player
                        && entity != player.getVehicle()  // 排除坐骑
        );

        for (EntityHitResult hit : hits) {
            Entity target = hit.getEntity();
            if (!(target instanceof LivingEntity)) continue;

            if (player instanceof SpearCooldownAccessor accessor) {
                if (accessor.WasRecentlyStabbed(target, getContactCooldownTicks())) {
                    continue;
                }
            }

            double targetSpeed = look.dot(SpearItem.getMotion(target));
            double relSpeed = Math.max(0.0, attackerSpeed - targetSpeed);

            // 用 Condition.test() 判断
            boolean canDismount = getDismountConditions().isPresent()
                    && getDismountConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);
            boolean canKnockback = getKnockbackConditions().isPresent()
                    && getKnockbackConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);
            boolean canDamage = getDamageConditions().isPresent()
                    && getDamageConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);

            if (canDismount || canKnockback || canDamage) {
                float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float damage = baseDamage + (float) Mth.floor(relSpeed * getDamageMultiplier());

                if (canDamage && target.hurt(player.damageSources().mobAttack(player), damage)) {
                    hitSomething = true;
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    player.setLastHurtMob(target);
                }
                if (canKnockback || (canDamage && hitSomething)) {
                    causeKnockback(player, target, 0.4F);
                }
                if (canDismount && target.isPassenger()) {
                    target.stopRiding();
                    hitSomething = true;
                }

                if (player instanceof SpearCooldownAccessor accessor) {
                    accessor.RememberStabbedEntity(target);
                }
            }
        }

        if (hitSomething) {
            player.level().broadcastEntityEvent(player, (byte) 2);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    getHitSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    // ========== 击退 ==========

    private void causeKnockback(Player player, Entity target, float strength) {
        if (strength <= 0.0F) return;
        float yRotRad = player.getYRot() * ((float) Math.PI / 180);
        if (target instanceof LivingEntity living) {
            living.knockback(strength, Mth.sin(yRotRad), -Mth.cos(yRotRad));
        } else {
            target.push(-Mth.sin(yRotRad) * strength, 0.1, Mth.cos(yRotRad) * strength);
        }
        player.setDeltaMovement(player.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        player.hurtMarked = false;
    }
    // ========== 重写原版行为 ==========

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean canAttackBlock(net.minecraft.world.level.block.state.BlockState state, Level level,
                                  net.minecraft.core.BlockPos pos, Player player) {
        return !player.isCreative();
    }
    // ========== 附魔/修复 ==========

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return getSpearEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return canRepair(stack, repairCandidate);
    }
}